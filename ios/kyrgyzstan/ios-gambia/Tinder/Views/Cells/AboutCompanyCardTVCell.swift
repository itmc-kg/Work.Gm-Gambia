//
//  AboutCompanyCardTVCell.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 10/14/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit

class AboutCompanyCardTVCell: UITableViewCell {

    @IBOutlet weak var aboutCompanyLabel: UILabel!
    
    var employer: Employer?{
        didSet{
            if let employer = employer{
                aboutCompanyLabel.text = employer.about
            }
        }
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    func setupWith(employer: Employer){
        self.employer = employer
    }
    
}
