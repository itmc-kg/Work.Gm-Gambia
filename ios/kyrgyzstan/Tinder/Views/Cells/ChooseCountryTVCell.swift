//
//  ChooseCountryTVCell.swift
//  Tinder
//
//  Created by Mairambek on 2/11/20.
//  Copyright © 2020 Azamat Kushmanov. All rights reserved.
//

import UIKit

class ChooseCountryTVCell: UITableViewCell {

    
    @IBOutlet weak var countryNameLabel: UILabel!
    @IBOutlet weak var countryImage: UIImageView!
    @IBOutlet weak var checkImage: UIImageView!
    
    override func awakeFromNib() {
        super.awakeFromNib()

        selectionStyle = .none
    }

}
