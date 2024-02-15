package ru.firstCrud.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.firstCrud.Models.Person;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
public class PersonDAO {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PersonDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public List<Person> index(){
        return jdbcTemplate.query("SELECT * from person", new BeanPropertyRowMapper<>(Person.class));
    }

    public void save(Person person){

        jdbcTemplate.update("INSERT into Person(name, age, email, address) VALUES (?, ?, ?, ?)", person.getName(), person.getAge(),
                person.getEmail(), person.getAddress());
    }

    public void update(int id, Person updatePerson){
        jdbcTemplate.update("UPDATE Person SET name=?, age=?, email=?, address=?, WHERE id=?", updatePerson.getName(),
                updatePerson.getAge(), updatePerson.getEmail(), updatePerson.getAddress(), id);
    }

    public void delete(int id){
        jdbcTemplate.update("delete from person where id=?", id);
    }

    public Person show(int id){
        return jdbcTemplate.query("SELECT * from person where id=?", new Object[]{id},
                        new BeanPropertyRowMapper<>(Person.class)).stream().findAny().orElse(null);
    }

    //теструем производительность пакетаной вставки
    public List<Person> create1000People(){
        List<Person> people = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            Random random = new Random();
            people.add(new Person(i, "name" + i, random.nextInt(80) + 1, "test" + i + "@mail.ru", "some address" ));
        }

        return people;
    }

    public Optional<Person> show(String email){
        return (jdbcTemplate.query("select * from Person Where email=?", new Object[]{email},
                new BeanPropertyRowMapper<>(Person.class)).stream().findAny());
    }

    public void testMultipleUpdate(){
       List<Person> people = create1000People();
       long before = System.currentTimeMillis();

        for (Person person: people) {
            jdbcTemplate.update("insert into person(name, age, email) values (?, ?, ?)", person.getName(),
                    person.getAge(), person.getEmail());
        }
       
       long after = System.currentTimeMillis();
        System.out.println("Time: " + (after-before));
       
    }

    public void testButchUpdate(){
        List<Person> people = create1000People();
        long before = System.currentTimeMillis();

        jdbcTemplate.batchUpdate("insert into person(name, age, email) values (?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                        preparedStatement.setString(1, people.get(i).getName());
                        preparedStatement.setInt(2, people.get(i).getAge());
                        preparedStatement.setString(3, people.get(i).getEmail());

                    }

                    @Override
                    public int getBatchSize() {
                        return people.size();
                    }
                });

        long after = System.currentTimeMillis();
        System.out.println("Time: " + (after-before));
    }


}
