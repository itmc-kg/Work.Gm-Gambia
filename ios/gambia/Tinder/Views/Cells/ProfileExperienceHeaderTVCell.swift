//
//  ProfileExperienceTVCell.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 9/24/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit

class ProfileExperienceHeaderTVCell: UITableViewCell {

    
    @IBOutlet weak var topGrayView: UIView!
    
    @IBOutlet weak var iconImageView: UIImageView!
    @IBOutlet weak var expierenceLabel: UILabel!
    
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    func setupAsEducation(){
        iconImageView.image = UIImage(named: "ic_education")
        expierenceLabel.text = "eigher_education".localized()
        topGrayView.isHidden = true
    }
    
    func setupAsExpierence(){
        iconImageView.image = UIImage(named: "ic_experience")
        expierenceLabel.text = "expiriance".localized()
        topGrayView.isHidden = false
    }
    
}
