package blue_duck

/*
open class simple_duck { //чтобы наследоваться от него, должен быть открытым
    open fun getColor() = "none" //открытая чтобы можно было переопределять
}
*/

/*
interface simple_duck { //интерфейс обязателен для переопределения
    //хранит только прототипы для функций
    fun getColor() : String //обязательно указываем возвращаемый тип данных
}
*/

abstract class simple_duck(var age: Int) {
    abstract fun getColor() : String
}

