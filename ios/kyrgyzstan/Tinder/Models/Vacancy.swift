//
//  Vacancy.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 10/2/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit
import ObjectMapper
import Firebase

enum VacancyStatus : String{
    case Opened = "opened"
    case Closed = "closed"
}

class Vacancy: Mappable {
    var id : String = ""
    var position : String = ""
    var creatorId : String = ""
    var description: String = ""
    var employmentTypeId : Int = 0
    var scheduleId : Int = 0
    var sphereId : String = ""
    var sphere : Sphere?
    var status : String = VacancyStatus.Opened.rawValue
    var salary : Salary?
    var createdAtTimeStamp: Timestamp = Timestamp()
    var applicationsCount : Int = 0
    var employer : Employer?
    var application : Application?
    var country : String = ""
    
    required convenience init?(map: Map) {
        self.init()
    }
    
    func mapping(map: Map) {
        
        id <- map["id"]
        position <- map["position"]
        creatorId <- map["creatorId"]
        description <- map["description"]
        employmentTypeId <- map["employmentTypeId"]
        scheduleId <- map["scheduleId"]
        sphereId <- map["sphereId"]
        status <- map["status"]
        salary <- map["salary"]
        applicationsCount <- map["applicationsCount"]
        country <- map["country"]

    }
    
    func toCreateParams() -> [String: Any]{
            var params = [
            "id" : id,
            "createdAt" : Timestamp(),
            "description" : description,
            "employmentTypeId" : employmentTypeId,
            "position" : position,
            "scheduleId" : scheduleId,
            "status" : status,
            "country" : UserDefaults.standard.string(forKey: Constants.USED_COUNTRY) ?? UsedCountry.Kyrgyzstan.rawValue
                
                ] as [String : Any]
        
        if let user = Auth.auth().currentUser{
            params["creatorId"] = user.uid
        }
        
        if let salary = self.salary{
            params["salary"] = salary.toCreateParams()
        }
        
        if let sphere = self.sphere{
            params["sphereId"] = sphere.id
        }
        
        return params
    }
}

enum SalaryType : String {
    case Range = "range"
    case Fixed = "fixed"
    case Undefined = "undefined"
}

class Salary : Mappable{
    var id : String = SalaryType.Fixed.rawValue
    var start : Int = Constants.SALARY_START
    var end : Int = Constants.SALARY_END
    
    required convenience init?(map: Map) {
        self.init()
    }
    
    func mapping(map: Map) {
        
        id <- map["id"]
        start <- map["start"]
        end <- map["end"]
    }
    
    func getSalary() -> String{
        switch id {
       
        case SalaryType.Range.rawValue:
            return "\(start * 1000) - \(end * 1000)"
        case SalaryType.Fixed.rawValue:
            let language = UserDefaults.standard.string(forKey: Constants.applicationCurrentLanguage) ?? Constants.RU_lang

            if language == "ky" {
                return "\(start * 1000) ден"
            }
            
            return "\("from".localized()) \(start * 1000)"
        case SalaryType.Undefined.rawValue:
            return "by_interview".localized()
        default:
            return ""
        }
    }
    
    
    func getSalaryType() -> String{
        switch id {
        case SalaryType.Range.rawValue:
            return "range_salary".localized()
        case SalaryType.Fixed.rawValue:
            return "fixed".localized()
        case SalaryType.Undefined.rawValue:
            return "not_indicated".localized()
        default:
            return ""
        }
    }
    
    func toCreateParams() -> [String: Any]{
        return [
            "id" : id,
            "start" : start,
            "end" : end
        ]
    }
}
