package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    // 스프링 라이프 사이클로 인해서 @Transactional과 @PostConstruct 어노테이션을 동시 적용이 안되기 때문에 각각 분리해서 코드를 구현해야합니다.
    @PostConstruct
    public void init(){
        initMemberService.init();
    }

    @Component
   static class InitMemberService{
       @PersistenceContext
       private EntityManager em;

       @Transactional
       public void init(){
           Team teamA = new Team("teamA");
           Team teamB = new Team("teamB");
           em.persist(teamA);
           em.persist(teamB);


           for (int i = 0; i < 100; i++) {
               Team selectedTeam = i % 2 == 0 ? teamA : teamB;
               em.persist(new Member("member" + i, i, selectedTeam));
           }
       }



   }



}
