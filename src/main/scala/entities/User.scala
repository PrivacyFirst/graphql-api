package entities

case class User(id: String,
                created: String,
                updated: String,
                userName: String,
                contactList: List[String])
    extends Entity
