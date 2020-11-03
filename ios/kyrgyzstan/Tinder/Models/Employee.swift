//
//  Employee.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 10/6/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit
import ObjectMapper

class Employee: Mappable {
    var id : String = ""
    var name : String = ""
    var imageUrl : String = ""
    var educations : [Education] = []
    var works : [Work] = []
    
    required convenience init?(map: Map) {
        self.init()
    }
    
    func mapping(map: Map) {
        
        id <- map["id"]
        name <- map["name"]
        imageUrl <- map["image"]
    }
    
    func getImageUrl() -> URL?{
        return URL(string: imageUrl)
    }
}
