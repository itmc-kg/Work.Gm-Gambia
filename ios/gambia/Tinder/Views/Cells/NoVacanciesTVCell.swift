//
//  NoVacanciesTVCell.swift
//  Tinder
//
//  Created by Mairambek on 2/9/20.
//  Copyright © 2020 Azamat Kushmanov. All rights reserved.
//

import UIKit

class NoVacanciesTVCell: UITableViewCell {

    @IBOutlet weak var noVacanciesLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        self.isUserInteractionEnabled = false
    }

}
