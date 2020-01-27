package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;
import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Transactional
@SpringBootTest
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;


    @BeforeEach
    public void before() {

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
    public void startJPQL() {
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
    public void resultFetch() throws Exception {

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
     * 단 2에서 회원 이름이 없으면 마지막 출력(nulls last)
     */

    @Test
    public void sort() throws Exception {
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

    @Test
    public void paging1() throws Exception {

        //given
        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        //then
        assertThat(result.size()).isEqualTo(2);

    }

    @Test
    public void paging2() throws Exception {

        //given
        //when
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();


        //then
        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);

    }


    @Test
    public void aggregation() throws Exception {

        //given
        //when
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        //then


        Tuple tuple = result.get(0);

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);

    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() throws Exception {

        //given
        //when
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        //then
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);


        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);


    }


    /**
     * 팀 A에 소속된 모든 회원을 조회
     */

    @Test
    public void join() throws Exception {

        //given
        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();


        //then
        // List 객체가 가지고 있는 Member 요소 중에서 Member 엔티티의 필드 명이 username 이면서 해당 필드에  포함된 값 있는지 검증할 때 사용합니다.
        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");

    }

    /**
     *
     * 세타 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     *
     */
    @Test
    public void theta_join() throws Exception {

        //given
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));


        //when
        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();


        //then
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");

     }

    /**
     *
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: select m, t from Member m left join m.team t on t.name = 'teamA'
     *
     */

    @Test
     public void join_on_filtering() throws Exception {

         //given
        //when
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .join(member.team, team)
                //.on(team.name.eq("teamA")) //inner join 시에는 where나 on 절 둘다 결과가 똑같기 때문에 익숙한 where절을 권장합니다. 외부조인이 필요시에만 사용함.
                .where(team.name.eq("teamA"))
                .fetch();


         //then


      }

    /**
     *
     * 연관관계 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     *
     */

    @Test
    public void join_on_no_relation() throws Exception {

        //given
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));


        //when
        // hibernate 5.1 부터 연관관계가 없는 엔티티끼리 외부조인 및 내부조인이 가능합니다. 주의할 점은 left join은 일반 조인과 다르게 엔티티 하나만 들어갑니다.
        // ex) 일반조인: leftJoin(member.team, team),  on 조인 : from(member).leftjoin(team).on(xxx)
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        //then
        for (Tuple tuple : result){
            System.out.println("tuple = " + tuple);
        }

    }


    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() throws Exception {

        //given
        em.flush();
        em.clear();


        //when
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();


        //then
        // 해당 엔티티가 초기화 되었는지 검증하는 메소
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();

    }


    @Test
    public void fetchJoinUse() throws Exception {

        //given
        em.flush();
        em.clear();


        //when
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();


        //then
        // 해당 엔티티가 초기화 되었는지 검증하는 메소드
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();

    }


    /**
     *  서브쿼리 JPAExpress 사용
     *  나이가 가장 많은 회원 조회
     */

    @Test
    public void subQuery() throws Exception {

        // 서브 쿼리 사용시에 별칭이 겹치면 안되기 때문에 QMember를 따로 생성해서 별칭을 구분해야 합니다.
        QMember memberSub = new QMember("memberSub");

        //given
        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        //then
        assertThat(result)
                .extracting("age")
                .containsExactly(40);

     }


    /**
     *  나이가 평균 이상인 회
     */

    @Test
    public void subQueryGoe() throws Exception {

        // 서브 쿼리 사용시에 별칭이 겹치면 안되기 때문에 QMember를 따로 생성해서 별칭을 구분해야 합니다.
        QMember memberSub = new QMember("memberSub");

        //given
        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        //then
        assertThat(result)
                .extracting("age")
                .containsExactly(30,40);

    }


    /**
     *  In 조
     */

    @Test
    public void subQueryIn() throws Exception {

        // 서브 쿼리 사용시에 별칭이 겹치면 안되기 때문에 QMember를 따로 생성해서 별칭을 구분해야 합니다.
        QMember memberSub = new QMember("memberSub");

        //given
        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        //then
        assertThat(result)
                .extracting("age")
                .containsExactly(20,30,40);

    }


    /**
     * select 절에서 sub 쿼리 사용 예제
     *
     */
    @Test
    public void selectSubQuery() throws Exception {

        QMember memberSub = new QMember("memberSub");

        //given
        List<Tuple> result = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();


        //when
        for (Tuple tuple : result){
            System.out.println("tuple = " + tuple);
        }

        //then
     }



}
