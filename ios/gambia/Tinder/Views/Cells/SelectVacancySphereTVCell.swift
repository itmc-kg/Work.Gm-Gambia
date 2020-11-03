//
//  SelectVacancySphereTVCell.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 9/27/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit

enum VacancyDetailType{
    case Sphere
    case Schedule
    case Salary
}

protocol SelectVacancySphereTVCellDelegate {
    func didSelect(cell: SelectVacancySphereTVCell)
}

class SelectVacancySphereTVCell: UITableViewCell {

    @IBOutlet weak var itemTitleLabel: UILabel!
    @IBOutlet weak var itemDescriptionLabel: UILabel!
    
    var type: VacancyDetailType = .Sphere
    var delegate: SelectVacancySphereTVCellDelegate?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    func setup(type: VacancyDetailType){
        self.type = type
        
        switch type {
        case .Sphere:
            itemTitleLabel.text = "sphere".localized()
            itemDescriptionLabel.text = "not_selected".localized()
            break
        case .Salary:
            itemTitleLabel.text = "salary".localized()
            itemDescriptionLabel.text = "not_selected".localized()
            break
        case .Schedule:
            itemTitleLabel.text = "schedule".localized()
            itemDescriptionLabel.text = "not_selected".localized()
            break
        }
    }
    
    open override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1, execute: {
            UIView.animate(withDuration: 0.1) {
                self.transform = CGAffineTransform.identity.scaledBy(x: 1.0, y: 1.0)
                self.alpha = 1.0
                
                if let delegate = self.delegate{
                    delegate.didSelect(cell: self)
                }
            }
        })
    }
    
    override open func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        UIView.animate(withDuration: 0.1) {
            self.transform = CGAffineTransform.identity.scaledBy(x: 0.97, y: 0.97)
            self.alpha = 0.5
        }
    }
    
    override func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) {
        UIView.animate(withDuration: 0.1) {
            self.transform = CGAffineTransform.identity.scaledBy(x: 1.0, y: 1.0)
            self.alpha = 1.0
        }
    }
    
}
