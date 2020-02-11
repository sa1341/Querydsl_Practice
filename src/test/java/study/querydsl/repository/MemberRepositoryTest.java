package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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


}
