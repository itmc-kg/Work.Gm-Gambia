//
//  CodeConfirmationVC.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 9/15/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit
import FirebaseAuth
import PKHUD

class CodeConfirmationVC: UIViewController {

    @IBOutlet weak var codeTextField: UITextField!
    @IBOutlet weak var descriptionLabel: UILabel!
    
    private let resendCodeView = UINib(nibName: "ResendCodeView",bundle: nil).instantiate(withOwner: nil, options: nil)[0] as! ResendCodeView
    
    var phoneNumber: String = ""
    var verificationId = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()

        descriptionLabel.text = "we_sent_code".localized() + "\(phoneNumber)" + "please_confirm".localized()
        self.view.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(hideKeyboard)))
        self.codeTextField.addTarget(self, action: #selector(textFieldDidChange(_:)), for: .editingChanged)
        
        HUD.dimsBackground = false
        HUD.allowsInteraction = false
        
        setupNavBar()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        self.codeTextField.becomeFirstResponder()
    }
    
    func setupNavBar(){
        
        let rect = CGRect(
            origin: CGPoint(x: 0, y: 0),
            size: CGSize(width: (self.navigationController?.navigationBar.frame.size.width)! - 150, height: (self.navigationController?.navigationBar.frame.size.height)!)
        )
        
        resendCodeView.setupWith(phoneNumber: self.phoneNumber)
        resendCodeView.delegate = self
        
        resendCodeView.frame = rect
        resendCodeView.isUserInteractionEnabled = true
        
        self.navigationItem.titleView?.isUserInteractionEnabled = true
        self.navigationItem.titleView = resendCodeView
    }
    
    @objc func textFieldDidChange(_ textField: UITextField) {
        if textField.text?.count == 6 {
            
            textField.resignFirstResponder()
            HUD.show(.progress)
            
            let credential = PhoneAuthProvider.provider().credential(
                withVerificationID: self.verificationId,
                verificationCode: self.codeTextField.text!)
            
            Auth.auth().signInAndRetrieveData(with: credential) { (user, error) in
                if let error = error {
                    HUD.hide()
                    self.codeTextField.shake()
                    self.codeTextField.becomeFirstResponder()
                    self.showAlertWithMessage(message: error.localizedDescription)
                    return
                }
                
                if let user = Auth.auth().currentUser{
                    print(user.uid, ">>>>>>>>>")
                    CloudStoreRefManager.instance.usersReferance
                        .document(user.uid)
                        .getDocument { (snapshot, error) in
                            if let error = error{
                                print("Firebase error: \(error.localizedDescription)")
                            }
                            if let data = snapshot?.data(), let role = data["role"] as? String{
                                
//                                if data["country"] as? String == "GM" {
//                                    Bundle.setLanguage(lang: "en")
//                                    UserDefaults.standard.set("GM", forKey: Constants.USED_COUNTRY)
//
//                                } else if  data["country"] as? String == "KG"{
//                                    Bundle.setLanguage(lang: "ru")
//                                    UserDefaults.standard.set("KG", forKey: Constants.USED_COUNTRY)
//                                }
                                
                                if role == EmployeeType.Employee.rawValue{
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
                                }else{
                                    UserDefaults.standard.set(EmployeeType.Employer.rawValue, forKey: Constants.USER_TYPE)
                                    
                                    let vc = UIStoryboard.init(name: Constants.EMPLOYER_STORYBOARD, bundle: nil).instantiateViewController(withIdentifier: "EmployerVacanciesVC") as! EmployerVacanciesVC
                                    UIApplication.shared.keyWindow?.rootViewController = UINavigationController(rootViewController: vc)
                                }
                            } else {
                                UIApplication.shared.keyWindow?.rootViewController = UINavigationController(rootViewController: SwitchEmployeTypeVC.newInstance())
                            }
                            
                            HUD.flash(.success)
                    }
                }
            }
        }
    }
    
    @objc func hideKeyboard(){
        codeTextField.resignFirstResponder()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}

extension CodeConfirmationVC : ResendCodeViewDelegate{
    func didUpdateVerificationId(verificationId: String) {
        self.verificationId = verificationId
    }
}
