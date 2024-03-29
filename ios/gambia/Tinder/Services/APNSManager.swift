//
//  APNSManager.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 10/11/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit
import FirebaseMessaging
import FirebaseAuth

class APNsManager: NSObject {
    static let instance = APNsManager()

    
    func setupTokens(){
        if let user = Auth.auth().currentUser{
            if let token = Messaging.messaging().fcmToken {
                if !token.isEmpty{
                    
                    print("token: \(token)")
                    
                    CloudStoreRefManager.instance.usersReferance
                        .document(user.uid)
                        .collection(CloudRout.devices.rawValue)
                        .document(token)
                        .setData(["type" : "ios"], merge: true)
                }
            }
        }
    }
    
    func removeToken(){
        if let user = Auth.auth().currentUser{
            if let token = Messaging.messaging().fcmToken {
                if !token.isEmpty{
                    
                    CloudStoreRefManager.instance.usersReferance
                        .document(user.uid)
                        .collection(CloudRout.devices.rawValue)
                        .document(token)
                        .delete()
                }
            }
        }
    }
}
