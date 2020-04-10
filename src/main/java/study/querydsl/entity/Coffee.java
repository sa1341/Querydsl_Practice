package study.querydsl.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Coffee {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private int price;

    @Builder
    public Coffee(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public static Coffee makeCoffee(String name, int price) {
        return Coffee.builder()
                .name(name)
                .price(price)
                .build();
    }

}
