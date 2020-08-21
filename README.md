# Hibernate equals() and hashCode()

### Демонстрация проблемы
Цель данного примера, продемонстрировать основную проблему использования `id` как основы для методов `equals()`/`hashCode()`.

Создадим с нуля проект через https://start.spring.io/ с использованием hibernate. 

##### build.gradle
```
dependencies {
   	implementation 'org.springframework.boot:spring-boot-starter-web'
   	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
   	runtimeOnly 'com.h2database:h2'
    ...
}
```

Создадим две сущности с привязкой `many-to-one`: `Cart` и `Items`.
##### Cart.java
```
public class Cart
        extends AbstractPersistable<Long> {
    ...
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    private Set<Items> items = new HashSet<>();
    
    ...
}    
```    
##### Items.java
```
public class Items
        extends AbstractPersistable<Long> {
    ...
    
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    
    ...
}    
```    

Обращаю внимание, что оба класса отнаследованы от класса `AbstractPersistable` из `spring-data-jpa` https://github.com/spring-projects/spring-data-jpa/blob/master/src/main/java/org/springframework/data/jpa/domain/AbstractPersistable.java, в котором как раз переопределены методы `equals()`/`hashCode()` на основе использования `id`.

Теперь попытаемся сохранить в базу 1 cart вместе с привязанным к нему 1 item-ом:
```
        Cart cart = new Cart("cart");
        Items item = new Items("item", cart);
        cart.getItems().add(item);


        logger.info("Item is inside the cart before save: {}", cart.getItems().contains(item));
        cart = cartRepository.save(cart);
        logger.info("Item is inside the cart after save: {}", cart.getItems().contains(item));
```
А теперь следите за руками: перед сохранением в базу и сразу после проверяем, находится ли item внутри cart-а.
```
2020-08-21 19:15:07.261  INFO 17912 --- [nio-8080-exec-1] r.m.equality.controller.CartController   : Item is inside the cart before save: true
2020-08-21 19:15:07.322  INFO 17912 --- [nio-8080-exec-1] r.m.equality.controller.CartController   : Item is inside the cart after save: false
```
Это действительно контринтуитивно, но item "пропал" из cart-а. На самом деле, item все ещё находится и является единственным элементов в Set-е `Cart.items`. Но, так как для этого объекта сгенерировался id-шник, и объекта поле `id` вместо `null`-а получило значение, то у объекта изменился `hashCode` и мы больше не можем найти его внутри `HashSet`-а, несмотря на то, что он всё ещё внутри.
Такое поведение может привести (и в реальности приводило) к проблемам и багам в продукте.
Поэтому, в качестве основы для методов `equals()`/`hashCode()` id подходит плохо. Лучше использовать business-ключ, который не меняется во время всего жизненного цикла объекта. Если всё же использовать id, то делать это надо крайне осторожно, помня про side-эфекты.
