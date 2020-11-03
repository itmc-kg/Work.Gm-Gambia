//
//  AppDelegate.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 9/10/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit
import Firebase
import IQKeyboardManagerSwift
import UserNotifications
import ObjectMapper
import FBSDKCoreKit
import FBSDKMarketingKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    
    var window: UIWindow?
    var appIsStarting = false

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        
        if let options = launchOptions, options[UIApplicationLaunchOptionsKey.remoteNotification] != nil{
            self.appIsStarting = true
        }
        
//        setupAppLanguage()
        registerForPushNotifications()
        configureFirebase()
        customizeNavigationBar()
        customizeTabbar()
        IQKeyboardManager.shared.enable = true
        ApplicationDelegate.shared.application(application, didFinishLaunchingWithOptions: launchOptions)

        APNsManager.instance.setupTokens()
        
        if #available(iOS 13.0, *) {
            window?.overrideUserInterfaceStyle = .light
        } else {
            // Fallback on earlier versions
        }

        Bundle.setLanguage(lang: "en")
        UserDefaults.standard.set(UsedCountry.Gambia.rawValue, forKey: Constants.USED_COUNTRY)
        return true
    }
    
    func setupAppLanguage() {
        if let lan = UserDefaults.standard.string(forKey: Constants.applicationCurrentLanguage){
            Bundle.setLanguage(lang: lan)
        }
    }
    
    func configureFirebase(){
        FirebaseApp.configure()
        let db = Firestore.firestore()
        let settings = db.settings
        db.settings = settings
    }
    
    func customizeNavigationBar(){
        UINavigationBar.appearance().tintColor = UIColor(hex:0x25324C)
        UINavigationBar.appearance().alpha = 1.0
        UINavigationBar.appearance().shadowImage = UIImage()
        UINavigationBar.appearance().barTintColor = UIColor.white
        UINavigationBar.appearance().backIndicatorTransitionMaskImage
            = UIImage(named: "ic_nav_back_button")
        UINavigationBar.appearance().backIndicatorImage = UIImage(named: "ic_nav_back_button")
    }
    
    func customizeTabbar(){
        UITabBar.appearance().tintColor = UIColor(hex:0x25324C)
    }
    
    func registerForPushNotifications() {
        
        UNUserNotificationCenter.current().delegate = self
        
        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .badge, .sound],
            completionHandler: {_, _ in })
        
        UIApplication.shared.registerForRemoteNotifications()
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler( [.alert, .badge, .sound])
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        
        Messaging.messaging().apnsToken = deviceToken
        APNsManager.instance.setupTokens()
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        
        print("Push: \(userInfo)")
        
        let state = application.applicationState

        if state == .inactive && self.appIsStarting{
            
            guard Auth.auth().currentUser != nil else{
                return
            }
            
            if let notification = Mapper<PushNotification>().map(JSON: userInfo as! [String: Any]){
                if notification.notificationType == PushNotificationType.NewMessage.rawValue{
                    if let vc = self.window!.rootViewController{
                        if vc.view != nil{
                            vc.openChat(chatId: notification.chatId)
                        }
                    }
                }
            }
            
            completionHandler(UIBackgroundFetchResult.newData)
        }
        
        Messaging.messaging().appDidReceiveMessage(userInfo)
    }
    
    func clearNotifications(_ application: UIApplication) {
        application.applicationIconBadgeNumber = 0
        let center = UNUserNotificationCenter.current()
        center.removeAllDeliveredNotifications()
        center.removeAllPendingNotificationRequests()
    }
    
    func application(_ app: UIApplication, open url: URL, options: [UIApplicationOpenURLOptionsKey : Any] = [:]) -> Bool {
        let handled = ApplicationDelegate.shared.application(app, open: url, options: options)

      // Add any custom logic here.
      
      return handled
    }
    
    func applicationWillResignActive(_ application: UIApplication) {
        self.appIsStarting = false
    }
    
    func applicationDidEnterBackground(_ application: UIApplication) {
        self.appIsStarting = false
    }
    
    func applicationWillEnterForeground(_ application: UIApplication) {
        self.appIsStarting = true
    }
    
    func applicationDidBecomeActive(_ application: UIApplication) {
        clearNotifications(application)
        self.appIsStarting = false
    }
    
    func applicationWillTerminate(_ application: UIApplication) {
        
    }
}

