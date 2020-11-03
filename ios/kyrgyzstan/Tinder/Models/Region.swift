//
//  Region.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 10/1/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit
import ObjectMapper

class Region: Mappable {
    var id : String = ""
    var country = ""
    
    var name: String {
        get {
            if usedCountry == UsedCountry.Gambia.rawValue {
                return en
            } else {
                if language == Constants.EN_lang {
                    return en
                } else if language == Constants.KG_lang {
                    return ky
                } else {
                    return ru
                }
            }
        }
    }
    
    var ru = ""
    var en = ""
    var ky = ""

    let language = UserDefaults.standard.string(forKey: Constants.applicationCurrentLanguage) ?? Constants.RU_lang

    let usedCountry = UserDefaults.standard.string(forKey: Constants.USED_COUNTRY) ?? UsedCountry.Kyrgyzstan.rawValue

    required convenience init?(map: Map) {
        self.init()
    }
    
    func mapping(map: Map) {
        
        id <- map["id"]
        ru <- map["ru"]
        en <- map["en"]
        ky <- map["ky"]
        country <- map["country"]

    }
    
}
