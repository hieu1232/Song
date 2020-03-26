package hieusenpaj.com.callface.`object`

data class Contacts(var name:String,
               var image:String,
               var status:String,
               var uid: String) {
    constructor() : this("","","","")
}