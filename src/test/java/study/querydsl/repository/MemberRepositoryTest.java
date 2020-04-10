package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class MemberRepositoryTest {


    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;


    @Test
    public void basicTest() throws Exception {

        //given
        Member member = new Member("member1", 10);

        //when
        memberRepository.save(member);

        //then
        Member findMember = memberRepository.findById(member.getId()).get();

        assertThat(findMember).isEqualTo(member);


        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).containsExactly(member);


        List<Member> result2 = memberRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);

    }



    @Test
    public void searchTest() throws Exception {

        //given
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


        MemberSearchCondition condition  = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        //when
        List<MemberTeamDto> result = memberRepository.search(condition);

        //then
        assertThat(result).extracting("username").containsExactly("member4");

    }

    @Test
    public void searchPageSimple() throws Exception {

        //given
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


      MemberSearchCondition condition = new MemberSearchCondition();

        PageRequest pageRequest = PageRequest.of(0, 3);

        //when
        Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, pageRequest);

        //then
        assertThat(result.getSize()).isEqualTo(3);

        assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3");

    }

    /**
     * 실무에서는 권장되지 않는 방법입니다.
     * 1. 서비스 계층이나 컨트롤러 계층에서 queydsl이라는 구현 기술에 대해 의존적이기 때문에 기술 교체 시 클라이언트 코드에 영향이 갈 수 있습니다.
     * 2. 묵시적 조인은 사용 가능하지만, letf join이 불가능 합니다.
     * 3. 복잡한 실무환경에서 사용하기에는 한계가 명확합니다.
     */

   @Test
    public void querydslPredicateExecutorTest() throws Exception {

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
        em.persist(member4);        //given
        QMember member = QMember.member;
        Iterable<Member> result = memberRepository.findAll(member.age.between(10, 40).and(member.username.eq("member1")));


        //when
        for (Member findMember : result){
            System.out.println("member = " + findMember);
        }


        //then
     }

}
