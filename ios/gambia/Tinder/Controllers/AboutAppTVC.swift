//
//  AboutAppTVC.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 9/16/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit

class AboutAppTVC: UITableViewController {

    @IBOutlet weak var versionLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String
        
        versionLabel.text = "V \(appVersion ?? "1.X")"
    }
    
}
