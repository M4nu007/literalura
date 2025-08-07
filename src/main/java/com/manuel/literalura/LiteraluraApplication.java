package com.manuel.literalura;

import com.manuel.literalura.Principal.Principal;
import com.manuel.literalura.repository.AutorRepository;
import com.manuel.literalura.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class LiteraluraApplication implements CommandLineRunner{

	@Autowired
	private AutorRepository repoAutor;
	@Autowired
	private LibroRepository repoLibro;

	public static void main(String[] args) {
		SpringApplication.run(LiteraluraApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Principal principal = new Principal(repoAutor, repoLibro);
		principal.mostrarMenu();
	}
}
