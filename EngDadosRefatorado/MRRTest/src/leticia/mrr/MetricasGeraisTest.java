package leticia.mrr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

 
 
import leticia.infra.DAO;
import leticia.mrr.ServicoMRR;

 

public class MetricasGeraisTest {
	private static SimpleDateFormat DF = new SimpleDateFormat("dd/MM/yyyy");

	@BeforeClass
	public static void init() {
		DAO.setPersistenceUnit("MRR");
	}
	
	
	@Before
	public void zeraBaze () throws SQLException {
		
		Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:testdb");
		conn.createStatement().execute("DROP SCHEMA PUBLIC CASCADE");
		conn.close();

	}

	@Test
	public void testaMetricas() throws ParseException {

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

		
 		Metrica[] metricas = servico.getMetricasEmpresa(201904);
		Assert.assertEquals(6, metricas.length);
		
		
		Metrica me = getMetrica(metricas,Tipo.MRR);
		Assert.assertEquals(1183.0, me.getValor(),0.001);
		
	    me = getMetrica(metricas,Tipo.New_MRR);
		Assert.assertEquals(100.0, me.getValor(),0.001);
		
	    me = getMetrica(metricas,Tipo.Expansion_MRR);
		Assert.assertEquals(214.0, me.getValor(),0.001);
		
	    me = getMetrica(metricas,Tipo.Contraction_MRR);
		Assert.assertEquals(65, me.getValor(),0.001);
		
		
	    me = getMetrica(metricas,Tipo.Cancelled_MRR);
		Assert.assertEquals(185.0, me.getValor(),0.001);
		
	    me = getMetrica(metricas,Tipo.Resurrected_MRR);
		Assert.assertEquals(399.0, me.getValor(),0.001);
		
		
	     
		 metricas = servico.getMetricasEmpresa(201903);
		Assert.assertEquals(6, metricas.length);
		
		
		 me = getMetrica(metricas,Tipo.MRR);
		Assert.assertEquals(720.0, me.getValor(),0.001);
		
	    me = getMetrica(metricas,Tipo.New_MRR);
		Assert.assertEquals(0, me.getValor(),0.001);
		
	    me = getMetrica(metricas,Tipo.Expansion_MRR);
		Assert.assertEquals(0, me.getValor(),0.001);
		
	    me = getMetrica(metricas,Tipo.Contraction_MRR);
		Assert.assertEquals(0, me.getValor(),0.001);
		
		
	    me = getMetrica(metricas,Tipo.Cancelled_MRR);
		Assert.assertEquals(0, me.getValor(),0.001);
		
	    me = getMetrica(metricas,Tipo.Resurrected_MRR);
		Assert.assertEquals(0, me.getValor(),0.001);
		

	}


	private Metrica getMetrica(Metrica[] metricas, Tipo tipo) {
		for (Metrica Metrica : metricas) {
			if (Metrica.getTipo() == tipo){
				return Metrica;
			}
		}
		return null;
	}

	 
}
