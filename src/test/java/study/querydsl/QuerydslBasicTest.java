package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

@Transactional
@SpringBootTest
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;


    @BeforeEach
    public void before(){

        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

    }


    @Test
    public void startJPQL(){
        // member1을 찾아라
        String qlString = "select m from Member m where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() throws Exception {

        //given

        //when
        // Static으로 import 해서 Q 타입을 사용하는게 가장 권장되고 깔끔한 방식입니다.
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")) // 파라미터 바인딩 처리, 생성자로 주는 값은 JPQL 실행 시에 주석으로 나오는 테이블 alias를 의미합니다.
                .fetchOne();
        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
     }
    
     @Test
     public void search() throws Exception {

         //given

         //when
         Member findMember = queryFactory
                 .selectFrom(member)
                 .where(member.username.eq("member1")
                         .and(member.age.eq(10)))
                 .fetchOne();

         //then
         assertThat(findMember.getUsername()).isEqualTo("member1");


      }


    @Test
    public void searchAndParam() throws Exception {

        //given
        //가독성이 좋고 깔끔한 방법이다.
        //when
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        (member.age.eq(10)))
                .fetchOne();

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() throws Exception{

     /*   List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();


        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();


        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();
*/
        //페이징용 쿼리이기 때문에 getTotal 메소드를 수행시에 count를 가져오기 때문에 쿼리가 2번 수행됩니다.
   /*     QueryResults<Member> result = queryFactory
                .selectFrom(member)
                .fetchResults();


        result.getTotal();
        List<Member> content = result.getResults();*/

        long total = queryFactory
                .selectFrom(member)
                .fetchCount();

    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     *단 2에서 회원 이름이 없으면 마지막 출력(nulls last)
     */

    @Test
    public void sort() throws Exception{
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();


        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }



}
