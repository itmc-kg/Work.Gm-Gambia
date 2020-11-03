//
//  SwitchEmployeTypeVC.swift
//  Tinder
//
//  Created by Avaz on 15/09/2018.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit
import FirebaseAuth

enum EmployeeType : String{
    case Employee = "employee"
    case Employer = "employer"
}

class SwitchEmployeTypeVC: UIViewController {
    static func newInstance() -> SwitchEmployeTypeVC{
        return UIStoryboard(name: Constants.LOGIN_STORYBOARD, bundle: nil).instantiateViewController(withIdentifier: "SwitchEmployeTypeVC") as! SwitchEmployeTypeVC
    }
    
    @IBOutlet weak var employerButton: UIButton!
    @IBOutlet weak var employeeButton: UIButton!

    
    //LAcalization Outlets
    @IBOutlet weak var iAmEmployeeLabel: UILabel!
    @IBOutlet weak var employeeLabel: UILabel!
    @IBOutlet weak var findVacanciesLabel: UILabel!
    @IBOutlet weak var iAmEmployerLabel: UILabel!
    @IBOutlet weak var employerLabel: UILabel!
    @IBOutlet weak var findSpecialistsLabel: UILabel!
    
    
    
    var usingCountry: String!
    override func viewDidLoad() {
        super.viewDidLoad()

        setupLanguage()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        self.navigationController?.isNavigationBarHidden = true
    }

    private func setupLanguage(){
        iAmEmployerLabel.text = "iam".localized()
        iAmEmployeeLabel.text = "iam".localized()
        employerLabel.text = "employerLabel".localized()
        employeeLabel.text = "employeeLabel".localized()
        findVacanciesLabel.text = "findVacanciesLabel".localized()
        findSpecialistsLabel.text = "findSpecialistsLabel".localized()
    }
    
    @IBAction func chooseEmployee(_ sender: UIButton) {
        guard let user = Auth.auth().currentUser else{
            return
            
        }
        
        CloudStoreRefManager.instance.usersReferance
            .document(user.uid)
            .setData(["role" : EmployeeType.Employee.rawValue,
                      "country": usingCountry]) { (error) in
                if let error = error{
                    self.showAlertWithMessage(message: error.localizedDescription)
                }else{
//        UIApplication.shared.keyWindow?.rootViewController = UIStoryboard.init(name: Constants.EMPLOYEE_STORYBOARD, bundle: nil).instantiateViewController(withIdentifier: "EmployeeTBC")
                    UIApplication.shared.keyWindow?.rootViewController = UINavigationController(rootViewController: SignupInfoTVC.newInstance())
                    
                }
        }
    }
    
    @IBAction func chooseEmployer(_ sender: UIButton) {
        
        guard let user = Auth.auth().currentUser else{
            return
            
        }

        CloudStoreRefManager.instance.usersReferance
            .document(user.uid)
            .setData(["role" : EmployeeType.Employer.rawValue,
                      "country": usingCountry]) { (error) in
                        print(user.uid, "<<<<<<")
                if let error = error{
                    self.showAlertWithMessage(message: error.localizedDescription)
                }else{
                    let vc = UIStoryboard.init(name: Constants.EMPLOYER_STORYBOARD, bundle: nil).instantiateViewController(withIdentifier: "CreateCompanyTVC")
                    UIApplication.shared.keyWindow?.rootViewController = UINavigationController(rootViewController: vc)
//                    let vc = UIStoryboard.init(name: Constants.EMPLOYER_STORYBOARD, bundle: nil).instantiateViewController(withIdentifier: "EmployerVacanciesVC") as! EmployerVacanciesVC
//                    UIApplication.shared.keyWindow?.rootViewController = UINavigationController(rootViewController: vc)
                }
        }
    }
}
