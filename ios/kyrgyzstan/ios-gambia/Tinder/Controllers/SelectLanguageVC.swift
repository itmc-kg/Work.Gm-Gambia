//
//  SelectLanguageVC.swift
//  Tinder
//
//  Created by Mairambek on 2/26/20.
//  Copyright © 2020 Azamat Kushmanov. All rights reserved.
//

import UIKit

class SelectLanguageVC: UIViewController {

    @IBOutlet weak var nextButton: UIButton!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var chooseLabguageLabel: UILabel!
    
    
    var selectedIndex: Int? {
        didSet {
            if selectedIndex != nil {
                self.nextButton.isEnabled = true
                tableView.reloadData()
            } else {
                self.nextButton.isEnabled = false
            }
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        nextButton.addTarget(self, action: #selector(nextTapped), for: .touchUpInside)
        self.nextButton.isEnabled = false
        setupTableView()
        setupOutlets()
    }
    
    func setupOutlets() {
        self.chooseLabguageLabel.text = "chooseLanguageLabel".localized()
        self.nextButton.setTitle("next".localized(), for: .normal)
    }
    
    func prepareAppFor(language: String) {
            Bundle.setLanguage(lang: language)
        }
    
    
    @objc func nextTapped() {
        UIApplication.shared.keyWindow?.rootViewController = UINavigationController(rootViewController: SwitchEmployeTypeVC.newInstance())
    }
    
    func setupTableView() {
        tableView.delegate = self
        tableView.dataSource = self
    }

}


extension SelectLanguageVC: UITableViewDelegate, UITableViewDataSource {
   
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 2
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "SelectLanguageTVCell", for: indexPath) as! SelectLanguageTVCell
        
        if indexPath.row == 0 {
            cell.languageImage.image = #imageLiteral(resourceName: "ic_kyrgyzstan")
            cell.languageNameLabel.text = "Кыргызча"
        } else {
            cell.languageImage.image = #imageLiteral(resourceName: "ic_russia")
            cell.languageNameLabel.text = "Русский"
        }
        
        if let index = selectedIndex, index == indexPath.row {
            cell.selectedImageView.isHidden = false
        } else {
            cell.selectedImageView.isHidden = true
        }
        
        return cell
        
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        
        return 56
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        if indexPath.row == 0 {
             Bundle.setLanguage(lang: "ky")
        } else {
             Bundle.setLanguage(lang: "ru")
        }
        
        setupOutlets()
        selectedIndex = indexPath.row
    }
    
    
}
