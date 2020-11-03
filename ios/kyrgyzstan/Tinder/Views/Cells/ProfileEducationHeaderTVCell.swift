//
//  ProfileEducationHeaderTVCell.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 9/24/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit

class ProfileEducationHeaderTVCell: UITableViewCell {

    @IBOutlet weak var profileImageView: UIImageView!
    
    @IBOutlet weak var nameLabel: UILabel!
    
    
    private var employee : Employee?{
        didSet{
            if let employee = employee{
                profileImageView.sd_setImage(with: employee.getImageUrl(), placeholderImage: Constants.placeholder, options: .continueInBackground, completed: nil)
                nameLabel.text = employee.name
            }
        }
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    
    func setupWith(employee: Employee){
        self.employee = employee
    }
    
}
