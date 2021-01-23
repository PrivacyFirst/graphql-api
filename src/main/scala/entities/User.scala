package entities

case class User(id: String,
                created: String,
                updated: String,
                firstName: String,
                middleName: String,
                surname: String)
    extends Entity
