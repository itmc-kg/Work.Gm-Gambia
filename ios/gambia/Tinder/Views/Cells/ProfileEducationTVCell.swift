//
//  ProfileEducationTVCell.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 9/24/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit

class ProfileEducationTVCell: UITableViewCell {

    @IBOutlet weak var educationNameLabel: UILabel!
    @IBOutlet weak var educationSpecialityLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    func setupWith(education: Education){
        educationNameLabel.text = education.name
        educationSpecialityLabel.text = education.speciality
    }
    
    func setupWith(work: Work){
        educationNameLabel.text = work.company
        educationSpecialityLabel.text = work.position
    }
}
