//
//  ChangeLanguageTVC.swift
//  Tinder
//
//  Created by Mairambek on 2/11/20.
//  Copyright © 2020 Azamat Kushmanov. All rights reserved.
//

import UIKit

class ChangeLanguageTVC: UITableViewController {

    @IBOutlet weak var ruCheckboxImage: UIImageView!
    @IBOutlet weak var kyCheckboxImage: UIImageView!
    @IBOutlet weak var chooseLanguageLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        setupCheckbox()
        chooseLanguageLabel.text = "chooseLanguageLabel".localized()
    }
    
    func setupCheckbox() {
        let text = "yes".localized()
        
        if text == "Да" {
            ruCheckboxImage.isHidden = false
            kyCheckboxImage.isHidden = true
        } else {
            ruCheckboxImage.isHidden = true
            kyCheckboxImage.isHidden = false
        }
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        switch indexPath.row {
        case 1:
            Bundle.setLanguage(lang: "ru")
            setupCheckbox()
            setupTabBarItemTitle()
            chooseLanguageLabel.text = "chooseLanguageLabel".localized()
        case 2:
            Bundle.setLanguage(lang: "ky")
            setupCheckbox()
            setupTabBarItemTitle()
            chooseLanguageLabel.text = "chooseLanguageLabel".localized()
        default:
            return
        }
    }
    
    private func setupTabBarItemTitle(){
        self.tabBarController?.tabBar.items?[0].title = "profileTitle".localized()
        self.tabBarController?.tabBar.items?[1].title = "searchTitle".localized()
        self.tabBarController?.tabBar.items?[2].title = "chatTitle".localized()
    }
    

  
}
