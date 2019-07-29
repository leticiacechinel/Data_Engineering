package leticia.mrr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import leticia.cliente.Cliente;
import leticia.infra.DAO;

public class CidadeEstadoSegmentoTest {
	@BeforeClass
	public static void init() {
		DAO.setPersistenceUnit("MRR");
	}
	
	
	@Before
	public void zeraBaze () throws SQLException {
		
		Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:testdb");
		conn.createStatement().execute("DROP SCHEMA PUBLIC CASCADE");
		conn.close();

		DAO.resetFactory();
	}
	
	@Test
	public void testaMetricasCidadeEstadoSegmento () throws ParseException {
		DAO dao = new DAO();
		dao.begin();
		
		Cliente c = new Cliente(1,"Cliente 1","Blumenau", "SC", "Projeto Mecânico");
		dao.persist(c);
		
		c = new Cliente(2,"Cliente 2","São Paulo", "SP", "Software");
		dao.persist(c);
		
		c = new Cliente(3,"Cliente 3","Florianópolis", "SC", "Construção Civil");
		dao.persist(c);
		
		c = new Cliente(4,"Cliente 4","Rio de Janeiro", "RJ", "Educação");
		dao.persist(c);
		
		c = new Cliente(5,"Cliente 5","São Paulo", "SP", "Educação");
		dao.persist(c);
		
		c = new Cliente(6,"Cliente 6","Blumenau", "SC", "Software");
		dao.persist(c);
		
		dao.commit();
		
		ServicoMRR servico = new ServicoMRR();

		servico.pagamentoEfetuado(1, "10/01/2019", "Bronze/4", 400);
		servico.pagamentoEfetuado(2, "12/01/2019", "Prata/3", 555);
		servico.pagamentoEfetuado(3, "11/01/2019", "Ouro/3", 750);
		servico.pagamentoEfetuado(5, "25/01/2019", "Ouro/1", 250);
		servico.pagamentoEfetuado(4, "10/02/2019", "Prata/2", 370);
		servico.pagamentoEfetuado(6, "09/04/2019", "Bronze/1", 100);
		servico.pagamentoEfetuado(2, "18/04/2019", "Platina/1", 399);
		servico.pagamentoEfetuado(3, "17/04/2019", "Prata/1", 185);
		servico.pagamentoEfetuado(5, "10/04/2019", "Platina/1", 399);
		
		
		
		//Métricas Cidade
		
		//Blumenau
		MetricaCidade[] mCidade = servico.getMetricasCidade("Blumenau", 201904);
		Assert.assertEquals(6,mCidade.length);
		MetricaCidade mc = getMetricaCidade(mCidade, Tipo.MRR);
		Assert.assertEquals(200.0, mc.getValor(),0.001);
		mc = getMetricaCidade(mCidade, Tipo.New_MRR);
		Assert.assertEquals(100.0, mc.getValor(),0.001);
		
	 		 
		//Gaspar
		mCidade = servico.getMetricasCidade("Gaspar", 201904);
		Assert.assertEquals(6,mCidade.length);

		
		//Métricas Estado
		MetricaEstado[] mEstado = servico.getMetricasEstado("SC", 201904);
		Assert.assertEquals(6,mCidade.length);

		MetricaEstado me = getMetricaEstado(mEstado, Tipo.MRR);
		Assert.assertEquals(385.0, me.getValor(),0.001);

		me = getMetricaEstado(mEstado, Tipo.New_MRR);
		Assert.assertEquals(100.0, me.getValor(),0.001);
		
		me = getMetricaEstado(mEstado, Tipo.Contraction_MRR);
		Assert.assertEquals(65.0, me.getValor(),0.001);
		
		
        //Métrica Segmento
		MetricaSegmento[] mSegmento = servico.getMetricasSegmento("Software", 201904);
		Assert.assertEquals(6,mSegmento.length);
		
		MetricaSegmento ms = getMetricaSegmento(mSegmento, Tipo.MRR);
		Assert.assertEquals(499.0, ms.getValor(),0.001);

		 ms = getMetricaSegmento(mSegmento, Tipo.New_MRR);
		Assert.assertEquals(100.0, ms.getValor(),0.001);

		 ms = getMetricaSegmento(mSegmento, Tipo.Expansion_MRR);
		Assert.assertEquals(214.0, ms.getValor(),0.001);


		//projeto mecanico
		mSegmento = servico.getMetricasSegmento("Projeto Mecânico", 201904);
		Assert.assertEquals(6,mCidade.length);
	    ms = getMetricaSegmento(mSegmento, Tipo.MRR);
		Assert.assertEquals(100.0, mc.getValor(),0.001);

		
		
		//Rio de Janeiro
		mCidade = servico.getMetricasCidade("Rio de Janeiro", 201904);
		Assert.assertEquals(6,mCidade.length);
	    mc = getMetricaCidade(mCidade, Tipo.Cancelled_MRR);
		Assert.assertEquals(185.0, mc.getValor(),0.001);

	}
	
	
	private MetricaSegmento getMetricaSegmento(MetricaSegmento[] metricas, Tipo tipo) {
		for (MetricaSegmento metrica : metricas) {
			if (metrica.getTipo() == tipo){
				return metrica;
			}
		}
		return null;
	}

	private MetricaCidade getMetricaCidade(MetricaCidade[] metricas, Tipo tipo) {
		for (MetricaCidade metrica : metricas) {
			if (metrica.getTipo() == tipo){
				return metrica;
			}
		}
		return null;
	}
	private MetricaEstado getMetricaEstado(MetricaEstado[] metricas, Tipo tipo) {
		for (MetricaEstado metrica : metricas) {
			if (metrica.getTipo() == tipo){
				return metrica;
			}
		}
		return null;
	}
}
