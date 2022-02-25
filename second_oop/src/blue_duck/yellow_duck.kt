package blue_duck

class yellow_duck(age: Int) : simple_duck(age) { //родительский класс со скобочками, без скобочек - интерфейс!
    override fun getColor() = "yellow" //надо переопределить потому что названия с родителем совпадают (open в родителе!)
}