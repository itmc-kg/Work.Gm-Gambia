//
//  SelectLanguageTVCell.swift
//  Tinder
//
//  Created by Mairambek on 2/26/20.
//  Copyright © 2020 Azamat Kushmanov. All rights reserved.
//

import UIKit

class SelectLanguageTVCell: UITableViewCell {

    @IBOutlet weak var languageImage: UIImageView!
    @IBOutlet weak var languageNameLabel: UILabel!
    @IBOutlet weak var selectedImageView: UIImageView!
    
    override func awakeFromNib() {
        super.awakeFromNib()

        selectionStyle = .none
    }

}
