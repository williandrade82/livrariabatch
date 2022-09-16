package br.com.fiap.rm344246.livrariabatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@EnableBatchProcessing
@SpringBootApplication
public class LivrariabatchApplication {

	Logger logger = LoggerFactory.getLogger(LivrariabatchApplication.class);

	public static void main(String[] args) { SpringApplication.run(LivrariabatchApplication.class, args); }

	@Bean
	public FlatFileItemReader<Livro> itemReader(@Value("${file.chunk}") Resource resource){
		return new FlatFileItemReaderBuilder<Livro>()
				.name("Livro item reader")
				.targetType(Livro.class)
				.resource(resource)
				.delimited().delimiter(";").names("titulo", "isbn")
				.build();
	}

	@Bean
	public ItemProcessor<Livro, Livro> itemProcessor(){
		return (livro) -> {
			// regra de negócio
			livro.setTitulo(livro.getTitulo().toUpperCase());
			livro.setIsbn(livro.getIsbn().trim());
			return livro;
		};
	}

	@Bean
	public JdbcBatchItemWriter<Livro> itemWriter(DataSource dataSource){
		return new JdbcBatchItemWriterBuilder<Livro>()
				.dataSource(dataSource)
				.sql("insert into TB_LIVRO (titulo, isbn) values (:titulo, :isbn)")
				.beanMapped()
				.build();
	}

	@Bean
	public Step step(
			StepBuilderFactory stepBuilderFactory,
			ItemReader<Livro> itemReader,
			ItemProcessor<Livro, Livro> itemProcessor,
			ItemWriter<Livro> itemWriter
	){
		return stepBuilderFactory.get("STEP - Chunk Step csv do JDBC")
				.<Livro, Livro>chunk(100)
				.reader(itemReader)
				.processor(itemProcessor)
				.writer(itemWriter)
				.build();
	}

	@Bean
	public Job job(
			JobBuilderFactory jobBuilderFactory,
			Step step
	){
		return jobBuilderFactory.get("JOB - Importar Livros")
				.start(step)
				.build();
	}
}


