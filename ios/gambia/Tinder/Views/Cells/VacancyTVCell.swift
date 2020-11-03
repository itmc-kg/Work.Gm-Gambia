//
//  VacancyTVCell.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 9/21/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit

class VacancyTVCell: UITableViewCell {

    @IBOutlet weak var companyImageView: UIImageView!
    @IBOutlet weak var companyNameLabel: UILabel!
    @IBOutlet weak var sphereLabel: UILabel!
    @IBOutlet weak var positionLabel: UILabel!
    @IBOutlet weak var scheduleLabel: UILabel!
    @IBOutlet weak var employmentTypeLabel: UILabel!
    @IBOutlet weak var aboutLabel: UILabel!
    
    private var vacancy: Vacancy?{
        didSet{
            if let vacancy = self.vacancy{
                positionLabel.text = vacancy.position
                aboutLabel.text = vacancy.description
                
                if let sphere = vacancy.sphere{
                    sphereLabel.text = sphere.name
                }
                
                if let employer = vacancy.employer{
                    companyNameLabel.text = employer.name
                    companyImageView.sd_setImage(with: employer.getImageUrl(), placeholderImage: Constants.placeholder, options: .continueInBackground, completed: nil)
                }
                
                let language = UserDefaults.standard.string(forKey: Constants.applicationCurrentLanguage) ?? Constants.RU_lang
               
                let scheduleIndex = min(vacancy.scheduleId, Constants.SHEDULE_NAMES.count - 1)
                let employmentTypeIndex = min(vacancy.employmentTypeId, Constants.EMPLPOYMENT_TYPES.count - 1)
                
                if language == "en" {
                    employmentTypeLabel.text = Constants.SHEDULE_NAMES_EN[scheduleIndex]
                    scheduleLabel.text = Constants.EMPLPOYMENT_TYPES_EN[employmentTypeIndex]
                } else if language == "ky" {
                    employmentTypeLabel.text = Constants.SHEDULE_NAMES_KG[scheduleIndex]
                    scheduleLabel.text = Constants.EMPLPOYMENT_TYPES_KG[employmentTypeIndex]
                } else {
                    employmentTypeLabel.text = Constants.SHEDULE_NAMES[scheduleIndex]
                    scheduleLabel.text = Constants.EMPLPOYMENT_TYPES[employmentTypeIndex]
                }
                
            }
        }
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    func setupWith(vacancy: Vacancy){
        self.vacancy = vacancy
    }
    
}
