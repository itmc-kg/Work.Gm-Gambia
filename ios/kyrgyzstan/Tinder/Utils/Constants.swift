//
//  Constants.swift
//  Tinder
//
//  Created by Azamat Kushmanov on 9/13/18.
//  Copyright © 2018 Azamat Kushmanov. All rights reserved.
//

import UIKit

struct Constants {
    
    static let LOGIN_STORYBOARD = "Login"
    static let MAIN_STORYBOARD = "Main"
    static let SIGNUP_STORYBOARD = "SignUp"
    static let SETTINGS_STORYBOARD = "Settings"
    static let EMPLOYEE_STORYBOARD = "Employee"
    static let EMPLOYER_STORYBOARD = "Employer"
    
    static let USER_TYPE = "UserType"
    
    static let iPhone5sSize: CGFloat = 568.0
    static let iPhone6sPlusSize: CGFloat = 736.0
    static let iPhoneXSize: CGFloat = 800.0
    static let iPhone6sSize: CGFloat = 667.0
    
    static let IMAGE_THUMBNAIL_QUALITY:CGFloat = 0.2
    
    static let EMPLPOYMENT_TYPES = ["Полная","Частичная","Стажировка","Временная","Удалённо"]
    static let EMPLPOYMENT_TYPES_EN = ["Full", "Partial", "Internship", "Temporary", "Remotely"]
    static let EMPLPOYMENT_TYPES_KG = ["Толук", "Толук эмес", "Стажировка", "Убактылуу", "Алыстан"]

    static let SHEDULE_NAMES = ["Гибкий","Будни","Волонтерство","Программы/Стипендии"]
    static let SHEDULE_NAMES_EN = ["Flexible", "Weekdays","Volunteering","Programs/scholarships"]
    static let SHEDULE_NAMES_KG = ["Ийкемдүү","Күндѳлүк турмуш","Волонтердук","Программалар/стипендиялар"]

    static let EDUCATION_TYPES = ["Высшее образование","Неполное высшее образование","Средне специальное образование","Среднее образование","Неполное среднее образование"]
   
    static let EDUCATION_TYPES_KG = ["Жогорку билим", "Толук эмес жогорку билим", "Орто кесиптик билим", "Орто билим", "Толук эмес орто билим"]
   
    static let EDUCATION_TYPES_EN = ["Higher education", "Incomplete higher education", "Secondary special education", "Secondary education", "Incomplete secondary education"]

    static let EDUCATION_TYPE_IDS = ["high_edu","nf_hight_edu","spec_edu","sec_edu","low_sec_edu"]
    
    static let SALARY_START = 5
    static let SALARY_END = 200
    
    static let VACANCIES_PAGINATION = 10
    static let APPLICATOINS_PAGINATION = 10
    
    static let FILTER_REGION_ALL_KG = "all_kg"
    static let FILTER_REGION_ALL_GM = "all_gm"

    static let FILTER_SELECTED_REGION_ID = "FILTER_SELECTED_REGION_ID"
    static let FILTER_SELECTED_SPHERE_IDS = "FILTER_SELECTED_SPHERE_IDS"
    static let FILTER_SELECTED_SALARY_START = "FILTER_SELECTED_SALARY_START"
    
    static let USED_COUNTRY = "USED_COUNTRY"
    static let applicationCurrentLanguage = "appCurrentLanguage"

    static let placeholder = UIImage(named: "ic_profile")
    
    static let EN_lang = "en"
    
    static let RU_lang = "ru"
    static let KG_lang = "ky"
}

enum UsedCountry: String{
    case Kyrgyzstan = "KG"
    case Gambia = "GM"
}

enum Language: String {
    case kg = "kg"
    case ru = "ru"
}
