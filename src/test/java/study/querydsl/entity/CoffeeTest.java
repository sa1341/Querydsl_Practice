package study.querydsl.entity;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Transactional
@SpringBootTest
public class CoffeeTest {

    @Autowired
    EntityManager em;

    @Test
    @Rollback(false)
    public void 커피객체_생성테스트() throws Exception{

        Coffee coffee1 = Coffee.makeCoffee("latte", 4000);
        Coffee coffee2 = Coffee.makeCoffee("Americano", 2800);
        Coffee coffee3 = Coffee.makeCoffee("Cafemoca", 4000);

        em.persist(coffee1);
        em.persist(coffee2);
        em.persist(coffee3);

    }


}
