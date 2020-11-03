//
//  AboutCompanyTVCell.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 9/24/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit
import SDWebImage

class AboutCompanyTVCell: UITableViewCell {

    
    @IBOutlet weak var companyImageView: UIImageView!
    @IBOutlet weak var companyNameLabel: UILabel!
    @IBOutlet weak var companyAboutLabel: UILabel!
   
    override func awakeFromNib() {
        super.awakeFromNib()
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    func setupWith(employer: Employer){
        companyNameLabel.text = employer.name
        companyAboutLabel.text = employer.about
        companyImageView.sd_setImage(with: employer.getImageUrl(),  placeholderImage: Constants.placeholder, options: .continueInBackground,completed: nil)
    }
    
}
