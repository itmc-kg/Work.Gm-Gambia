//
//  ChooseCountryVC.swift
//  Tinder
//
//  Created by Avaz on 6/25/19.
//  Copyright © 2019 Azamat Kushmanov. All rights reserved.
//

import UIKit


class ChooseCountryVC: UIViewController {
  
    static func newInstance() -> ChooseCountryVC{
        return UIStoryboard(name: Constants.LOGIN_STORYBOARD, bundle: nil).instantiateViewController(withIdentifier: "ChooseCountryVC") as! ChooseCountryVC
    }
    
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var continueOutlet: UIButton!
    
    var selectedIndex: Int? {
        didSet {
            if let index = selectedIndex {
                continueOutlet.isEnabled = true
                if index == 0 {
                    prepareAppFor(country: .Kyrgyzstan)
                } else {
                    prepareAppFor(country: .Gambia)
                }
            }
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupTableView()
        countryLabel.text = "country".localized()
        continueOutlet.isEnabled = false
    }
    
    @IBOutlet weak var countryLabel: UILabel!
 
    func prepareAppFor(country: UsedCountry) {
        UserDefaults.standard.set(country.rawValue, forKey: Constants.USED_COUNTRY)
        if country == .Kyrgyzstan{
            Bundle.setLanguage(lang: "ru")
        } else {
            Bundle.setLanguage(lang: "en")
        }
    }
    
    func setupTableView(){
        tableView.delegate = self
        tableView.dataSource = self
    }
    
    @IBAction func continueButton(_ sender: UIButton) {

        if selectedIndex == 0 {
            prepareAppFor(country: .Kyrgyzstan)
            let shooseLanguageVC = UIStoryboard.init(name: "Login", bundle: nil).instantiateViewController(withIdentifier: "SelectLanguageVC") as! SelectLanguageVC
            UIApplication.shared.keyWindow?.rootViewController = UINavigationController(rootViewController: shooseLanguageVC)
        } else {
            prepareAppFor(country: .Gambia)
            UIApplication.shared.keyWindow?.rootViewController = UINavigationController(rootViewController: SwitchEmployeTypeVC.newInstance())
        }
        
    }
        
}

extension ChooseCountryVC: UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 2
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "ChooseCountryTVCell", for: indexPath) as! ChooseCountryTVCell
        
        if indexPath.row == 0 {
            cell.countryNameLabel.text = "Кыргызстан"
            cell.countryImage.image = #imageLiteral(resourceName: "ic_kyrgyzstan")
        } else {
            cell.countryNameLabel.text = "Gambia"
            cell.countryImage.image = #imageLiteral(resourceName: "ic_gambia")
        }
        
        if let index = selectedIndex, index == indexPath.row {
            cell.checkImage.isHidden = false
        } else {
            cell.checkImage.isHidden = true
        }
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        
        return 90
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        selectedIndex = indexPath.row
        tableView.reloadData()
    }
}

