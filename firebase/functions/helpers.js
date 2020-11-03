const async = require('async');
var send = async function(admin, devices, payload, options) {
  if(devices.length == 0) {
    return;
  }
  const response = await admin.messaging().sendToDevice(devices.map(e => e.id), payload, options)
  var removeEvents = [];
  response.results.forEach((result, index) => {
    const error = result.error;
    if(error) {
      console.log(error.code);
      // Cleanup the tokens who are not registered anymore.
      if (error.code === 'messaging/invalid-registration-token' ||
          error.code === 'messaging/registration-token-not-registered') {
        const invalidToken = devices[index];
        removeEvents.push(invalidToken.ref.delete());
      }
    }
  });
  await Promise.all(removeEvents);
  return;
}
module.exports = admin => {
  const helpers = {
    sendNotification: ({receiverId, type, devices, payload}) => {
      return admin.firestore().collection('users').doc(receiverId).get()
      .then(userSnap => {
        if(userSnap.data()[type] === false) {
          return;
        }
        var android = [];
        var ios = [];
        devices.forEach(device => {
          if(device.data().type === 'android') {
            android.push(device);
          } else {
            ios.push(device);
          }
        });
        return Promise.all([
          send(admin, android, {data: payload.data}, { priority: 'high' }),
          send(admin, ios, payload, { contentAvailable: true })
        ]);
      });
    }
  };
  return helpers;
}
