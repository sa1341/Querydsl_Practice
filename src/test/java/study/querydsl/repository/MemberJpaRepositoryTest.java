package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class MemberJpaRepositoryTest {

    
    @Autowired
    EntityManager em;
    
    @Autowired
    MemberJpaRepository memberJpaRepository;


    @Test
    public void basicTest() throws Exception {

        //given
        Member member = new Member("member1", 10);

        //when
        memberJpaRepository.save(member);

        //then
        Member findMember = memberJpaRepository.findById(member.getId()).get();

        assertThat(findMember).isEqualTo(member);


        List<Member> result1 = memberJpaRepository.findAll_Querydsl();
        assertThat(result1).containsExactly(member);


        List<Member> result2 = memberJpaRepository.findByUsername_Querydsl("member1");
        assertThat(result2).containsExactly(member);

    }
    
}
