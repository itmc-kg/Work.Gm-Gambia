const functions = require('firebase-functions');
const admin = require('firebase-admin');
const async = require('async');

var serviceAccount = require('./serviceKey.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://job-153a4.firebaseio.com'
});
firestore = admin.firestore();

const helpers = require('./helpers')(admin);

exports.newAppNotification = functions.firestore.document('applications/{applicationId}')
.onCreate(async (snap, context) => {
  const appData = snap.data();
  const applicationId = context.params.applicationId;
  const [authorSnap, employerDevices] = await Promise.all([
    firestore.collection('employees').doc(appData.authorId).get(),
    firestore.collection('users').doc(appData.employerId).collection('devices').get()
  ]);
  const authorData = authorSnap.data();
  const message = {
    data: {
      notificationType: "new/app",
      applicationId: applicationId,
      employeeName: authorData.name,
      employeeAvatar: authorData.image
    },
    notification: {
      title: "New Application",
      body: appData.position
    }
  };
  const incrementAppCount = firestore.runTransaction(function(transaction) {
    var vacancyRef = firestore.collection('vacancies').doc(appData.vacancyId);
    return transaction.get(vacancyRef).then(function(vacancy) {
      if (!vacancy.exists) {
          throw "Document does not exist!";
      }
      var count = vacancy.data().applicationsCount;
      var newCount = Number.isInteger(count) ? count + 1 : 1;
      transaction.update(vacancyRef, { applicationsCount: newCount });
    });
  });
  return Promise.all([
    helpers.sendNotification({ receiverId: appData.employerId, type: "application_notifications", devices: employerDevices.docs, payload: message }),
    incrementAppCount
  ]);
});

exports.acceptAppNotification = functions.firestore.document('applications/{applicationId}')
.onUpdate(async (change, context) => {
  const appData = change.after.data();
  const prevAppData = change.before.data();
  const applicationId = context.params.applicationId;
  if(appData.status === prevAppData.status) {
    return Promise.resolve();
  }
  var tasks = [];
  if(appData.status === 'accepted') {
    const [employerSnap, authorDevices] = await Promise.all([
      firestore.collection('employers').doc(appData.employerId).get(),
      firestore.collection('users').doc(appData.authorId).collection('devices').get()
    ]);
    const employerData = employerSnap.data();
    const message = {
      data: {
        notificationType: "app/accepted",
        applicationId: applicationId,
        employerName: employerData.name,
        employerAvatar: employerData.image
      },
      notification: {
        title: "Application accepted",
        body: appData.position
      }
    };
    tasks.push(helpers.sendNotification({ receiverId: appData.authorId, type: "application_notifications", devices: authorDevices.docs, payload: message }))
  }
  var changeAppCount = firestore.runTransaction(function(transaction) {
    var vacancyRef = firestore.collection('vacancies').doc(appData.vacancyId);
    return transaction.get(vacancyRef).then(function(vacancy) {
      if (!vacancy.exists) {
          throw "Document does not exist!";
      }
      const count = vacancy.data().applicationsCount;
      var newCount = 0;
      if(appData.status === 'created') {
        newCount = Number.isInteger(count) ? count + 1 : 1;
      } else {
        newCount = Number.isInteger(count) ? count - 1 : 0;
      }
      transaction.update(vacancyRef, { applicationsCount: newCount });
    });
  });
  tasks.push(changeAppCount);
  Promise.all(tasks);
});

exports.newMessageTrigger = functions.firestore.document('chats/{chatId}/messages/{messageId}')
.onWrite((change, context) => {
  const chatId = context.params.chatId;
  if(!change.after.exists) {
    return Promise.resolve();
  }
  const messageData = change.after.data();
  const messageId = context.params.messageId;
  return Promise.all([
    firestore.collection('chats').doc(chatId).get(),
    firestore.collection('users').doc(messageData.to).collection('devices').get()
  ]).then(([chatSnap, devices]) => {
    const chatData = chatSnap.data();
    const message = {
      data: {
        notificationType: "new/message",
        messageId: messageId,
        chatId: chatId,
        content: messageData.content,
        fromName: chatData[messageData.from].name,
        fromAvatar: chatData[messageData.from].image
      },
      notification: {
        title: chatData[messageData.from].name,
        body: messageData.content
      }
    };
    var tasks = [];
    if(!change.before.exists) {
      tasks.push(helpers.sendNotification({ receiverId: messageData.to, type: "message_notifications", devices: devices.docs, payload: message }));
    }
    tasks.push(firestore.collection('chats').doc(chatId).update({
      lastMessage: messageData,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    }));
    return Promise.all(tasks);
  });
});

var updateChat = (change, context) => {
  const userData = change.after.data();
  const previousUserData = change.before.data();
  const userId = context.params.userId;
  if(!((userData.name !== previousUserData.name) || (userData.image !== previousUserData.image))) {
    return Promise.resolve();
  }
  return firestore.collection('chats')
  .where('users', 'array-contains', userId)
  .get()
  .then(querySnap => {
    const chats = querySnap.docs;
    return new Promise((resolve, reject) => {
      async.each(chats, (chat, db) => {
        var update = {};
        update[userId] = {
          name: userData.name,
          image: userData.image
        };
        chat.ref.update(update).then((sm) => {return cb(null)}).catch((err) => { return cb(err) })
      }, err => {
        if(err) {
          return reject(err)
        }
        resolve();
      });
    });
  });
}
exports.updateChatEmployees = functions.firestore.document('employees/{userId}')
.onUpdate(updateChat);
exports.updateChatEmployers = functions.firestore.document('employers/{userId}')
.onUpdate(updateChat);
