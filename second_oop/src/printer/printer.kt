package printer

import blue_duck.simple_duck

class printer {
    fun printDuck(duck: simple_duck, type: String) {
        val printerStrategyFactory = printer_strategy_factory()
        val strategy = printerStrategyFactory.create(type)

        strategy.print_duck(duck)
    }
}