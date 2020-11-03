//
//  Sphere.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 9/28/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit
import ObjectMapper

class Sphere: NSObject, Mappable {
    var id : String = ""
    var ru = ""
    var en = ""
    var ky = ""
    let language = UserDefaults.standard.string(forKey: Constants.applicationCurrentLanguage) ?? Constants.RU_lang
    
    var name: String {
        get {
            if language == Constants.EN_lang {
                return en
            } else if language == Constants.KG_lang {
                return ky
            } else {
                return ru
            }
        }
    }

    
    required convenience init?(map: Map) {
        self.init()
    }
    
    func mapping(map: Map) {
        
        id <- map["id"]
        ru <- map["ru"]
        en <- map["en"]
        ky <- map["ky"]
    }
    
    
    override func isEqual(_ object: Any?) -> Bool {
        if let sphere = object as? Sphere {
            return self.id == sphere.id
        } else {
            return false
        }
    }
}
