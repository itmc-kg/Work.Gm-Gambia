//
//  SettingsTVC.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 9/16/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit
import FirebaseAuth

class SettingsTVC: UITableViewController {

    @IBOutlet weak var messageSwitch: UISwitch!
    @IBOutlet weak var applicationSwitch: UISwitch!
//    @IBOutlet weak var languageLabel: UILabel!
    
    //Become employer or employee
    @IBOutlet weak var changeRoleLabel: UILabel!
    
    //Language setup Outlets
    @IBOutlet weak var settingsTitle: UILabel!
    @IBOutlet weak var newMessagesLabel: UILabel!
    @IBOutlet weak var aboutNewMessages: UILabel!
    @IBOutlet weak var answersLabel: UILabel!
    @IBOutlet weak var aboutAnswersLabel: UILabel!
    @IBOutlet weak var termsOfUseLabel: UILabel!
    @IBOutlet weak var aboutAppLabel: UILabel!
    @IBOutlet weak var logoutLabel: UILabel!
//    @IBOutlet weak var languageSetupLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
//        let country = UserDefaults.standard.string(forKey: Constants.USED_COUNTRY) ?? UsedCountry.Kyrgyzstan.rawValue
        
//        if country == UsedCountry.Gambia.rawValue {
//            self.languageLabel.isHidden = true
//            self.languageSetupLabel.isHidden = true
//        }
//
        navigationItem.title = " "
        self.tableView.tableFooterView = UIView(frame: .zero)
        updateSettings()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        languageSetup()
        self.navigationController?.isNavigationBarHidden = false
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return section == 0 ? CGFloat.leastNormalMagnitude : 10
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        
        guard indexPath.section == 1 else {
            return
        }
        
        switch indexPath.row {
        case 0:
            userAgreements()
            break
        case 1:
            aboutApp()
            break
        case 2:
            changeUserRole()
            break
        case 3:
            
            logout()
//            let country = UserDefaults.standard.string(forKey: Constants.USED_COUNTRY) ?? UsedCountry.Kyrgyzstan.rawValue
//            if country == UsedCountry.Gambia.rawValue {
//                return
//            }
//
//            changeLanguage()
            break
//        case 4:
//            logout()
//            break
        default:
            break
        }
    }
    
    func updateSettings(){
        if let user = Auth.auth().currentUser{
            CloudStoreRefManager.instance.usersReferance
                .document(user.uid)
                .getDocument(completion: { (snapshot, error) in
                    if let snapshot = snapshot, let json = snapshot.data(){
                        if let messageNotificationsEnable = json["message_notifications"] as? Bool{
                            self.messageSwitch.isOn = messageNotificationsEnable
                        }
                        
                        if let applicationNotificationsEnable = json["application_notifications"] as? Bool{
                            self.applicationSwitch.isOn = applicationNotificationsEnable
                        }
                    }
                })
            
        }
    }
    
    func changeLanguage() {
        
        let languageVC = UIStoryboard.init(name: Constants.SETTINGS_STORYBOARD, bundle: nil).instantiateViewController(withIdentifier: "ChangeLanguageTVC") as! ChangeLanguageTVC
        navigationController?.pushViewController(languageVC, animated: true)
    }
    
    func aboutApp() {
        setupBackButton()
        
        let vc = UIStoryboard(name: Constants.SETTINGS_STORYBOARD, bundle: nil).instantiateViewController(withIdentifier: "AboutAppTVC") as! AboutAppTVC
        self.navigationController?.pushViewController(vc, animated: true)
    }
    
    func userAgreements() {
        setupBackButton()
        
        let vc = UIStoryboard(name: Constants.SETTINGS_STORYBOARD, bundle: nil).instantiateViewController(withIdentifier: "UserAgreementsTVC") as! UserAgreementsTVC
        self.navigationController?.pushViewController(vc, animated: true)
    }
    
    @IBAction func messageNotificationsChanged(_ sender: UISwitch) {
        
        if let user = Auth.auth().currentUser{
            CloudStoreRefManager.instance.usersReferance
                .document(user.uid)
                .setData(["message_notifications": sender.isOn], merge: true)
            
            updateSettings()
        }
    }
    
    
    @IBAction func applicationNotificationsChanged(_ sender: UISwitch) {
        if let user = Auth.auth().currentUser{
            CloudStoreRefManager.instance.usersReferance
                .document(user.uid)
                .setData(["application_notifications": sender.isOn], merge: true)
            
            updateSettings()
        }
    }
    
    func logout() {
        let alert = UIAlertController(title: "logout".localized(), message: "", preferredStyle: UIAlertControllerStyle.alert)
        alert.addAction(UIAlertAction(title: "cancel".localized(), style: UIAlertActionStyle.default, handler: nil))
        alert.addAction(UIAlertAction(title: "yes".localized(), style: .destructive, handler: { (alert) in
            let firebaseAuth = Auth.auth()
            do {
                APNsManager.instance.removeToken()
                try firebaseAuth.signOut()
                UserDefaults.standard.removeObject(forKey: Constants.applicationCurrentLanguage)
                UserDefaults.standard.removeObject(forKey: Constants.USED_COUNTRY)
                UserDefaults.standard.removeObject(forKey: Constants.USER_TYPE)

            } catch let signOutError as NSError {
                self.showAlertWithMessage(message: signOutError.localizedDescription)
            }
        }))
        self.present(alert, animated: true, completion: nil)
    }
    
    func changeUserRole(){
        let changeRoleAlert = UIAlertController.init(title: "", message: "changeRoleAlert".localized(), preferredStyle: .alert)
        let yesAction = UIAlertAction.init(title: "yes".localized(), style: .default) { (UIAlertAction) in
            
            if let user = Auth.auth().currentUser{
                
                if let userType = UserDefaults.standard.string(forKey: Constants.USER_TYPE) {
                    
                    if userType == EmployeeType.Employee.rawValue {
                        
                        DispatchQueue.main.async {
                            self.setUserRole(userType: EmployeeType.Employer.rawValue)
                        }
                        
                        BDManager.instance.getEmployer(employerId: user.uid, completion: { (status, employer) in
                            if status == .Failed{
                                let vc = UIStoryboard.init(name: Constants.EMPLOYER_STORYBOARD, bundle: nil).instantiateViewController(withIdentifier: "CreateCompanyTVC")
                                UIApplication.shared.keyWindow?.rootViewController = UINavigationController(rootViewController: vc)
                                return
                            }
                            
                            if status == .Success{
                                if employer.name.isEmpty{
                                    let vc = UIStoryboard.init(name: Constants.EMPLOYER_STORYBOARD, bundle: nil).instantiateViewController(withIdentifier: "CreateCompanyTVC")
                                    UIApplication.shared.keyWindow?.rootViewController = UINavigationController(rootViewController: vc)
                                    return
                                }
                            }
                            UserDefaults.standard.set(EmployeeType.Employer.rawValue, forKey: Constants.USER_TYPE)
                            
                            let vc = UIStoryboard.init(name: Constants.EMPLOYER_STORYBOARD, bundle: nil).instantiateViewController(withIdentifier: "EmployerVacanciesVC")
                            UIApplication.shared.keyWindow?.rootViewController = UINavigationController(rootViewController: vc)
                        })
                    } else {
                        
                        DispatchQueue.main.async {
                            self.setUserRole(userType: EmployeeType.Employee.rawValue)
                        }
                        
                        BDManager.instance.getEmployee(employeeId: user.uid, completion: { (status, employee) in
                            
                            if status == .Failed{
                                UIApplication.shared.keyWindow?.rootViewController = UINavigationController(rootViewController: SignupInfoTVC.newInstance())
                                return
                            }
                            
                            if status == .Success{
                                if employee.name.isEmpty{
                                    UIApplication.shared.keyWindow?.rootViewController = UINavigationController(rootViewController: SignupInfoTVC.newInstance())
                                    return
                                }
                            }
                            
                            UserDefaults.standard.set(EmployeeType.Employee.rawValue, forKey: Constants.USER_TYPE)
                            
                            UIApplication.shared.keyWindow?.rootViewController = UIStoryboard.init(name: Constants.EMPLOYEE_STORYBOARD, bundle: nil).instantiateViewController(withIdentifier: "EmployeeTBC")
                        })
                    }
                }
            }
        }
        
        let cancel = UIAlertAction.init(title: "cancel".localized(), style: .default, handler: nil)
        changeRoleAlert.addAction(cancel)
        changeRoleAlert.addAction(yesAction)

        present(changeRoleAlert, animated: true, completion: nil)
    }
    
    func setUserRole(userType: String ){
        guard let user = Auth.auth().currentUser else { return }
        CloudStoreRefManager.instance.usersReferance
            .document(user.uid)
            .setData(["role" : userType,
                      "country": UsedCountry.Gambia.rawValue]) { (error) in
                        print(user.uid, "<<<<<<")
                        if let error = error {
                            self.showAlertWithMessage(message: error.localizedDescription)
                            return
                        }
        }
    }
    
    func languageSetup(){
        newMessagesLabel.text = "newMessagesLabel".localized()
        aboutNewMessages.text = "aboutNewMessages".localized()
        answersLabel.text = "answersLabel".localized()
//        self.languageLabel.text = "language".localized()
        aboutAnswersLabel.text = "aboutAnswersLabel".localized()
        termsOfUseLabel.text = "termsOfUseLabel".localized()
        aboutAppLabel.text = "aboutAppLabel".localized()
        logoutLabel.text = "logoutLabel".localized()
//        languageSetupLabel.text = "languageSetupLabel".localized()
        settingsTitle.text = "settingsTitle".localized()

        if let userType = UserDefaults.standard.string(forKey: Constants.USER_TYPE) {
            print(userType)
            changeRoleLabel.text = userType.localized()
        } else {
            changeRoleLabel.text = "User Type Error"
        }
    }
}
