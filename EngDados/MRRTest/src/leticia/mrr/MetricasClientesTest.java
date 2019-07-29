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

 

public class MetricasClientesTest {
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

		DAO.resetFactory();
	}

	@Test
	public void testaInclusao() throws ParseException {

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

		MetricaCliente[] metricas;
		MetricaCliente metrica;

		// Cliente 1, Abril/2019
		metricas = servico.getMetricas(1, 201904);
		Assert.assertEquals(1, metricas.length);

		metrica = metricas[0];
		Assert.assertEquals(Tipo.MRR, metrica.getTipo());
		Assert.assertEquals(100.0, metrica.getValor(), 0.0001);

		// Cliente 6, Abril/2019
		metricas = servico.getMetricas(6, 201904);
		Assert.assertEquals(2, metricas.length);

	
		metrica = metricas[1];
		Assert.assertEquals(Tipo.MRR, metrica.getTipo());
		Assert.assertEquals(100.0, metrica.getValor(), 0.0001);

		metrica = metricas[0];
		Assert.assertEquals(Tipo.New_MRR, metrica.getTipo());
		Assert.assertEquals(100.0, metrica.getValor(), 0.0001);

		// Cliente 2, Abril/2019
		metricas = servico.getMetricas(2, 201904);
		Assert.assertEquals(2, metricas.length);

		metrica = metricas[1];
		Assert.assertEquals(Tipo.MRR, metrica.getTipo());
		Assert.assertEquals(399.0, metrica.getValor(), 0.0001);

		metrica = metricas[0];
		Assert.assertEquals(Tipo.Expansion_MRR, metrica.getTipo());
		Assert.assertEquals(214.0, metrica.getValor(), 0.0001);

		// Cliente 3, Abril/2019
		metricas = servico.getMetricas(3, 201904);
		Assert.assertEquals(2, metricas.length);

		metrica = metricas[1];
		Assert.assertEquals(Tipo.MRR, metrica.getTipo());
		Assert.assertEquals(185.0, metrica.getValor(), 0.0001);

		metrica = metricas[0];
		Assert.assertEquals(Tipo.Contraction_MRR, metrica.getTipo());
		Assert.assertEquals(65, metrica.getValor(), 0.0001);

		// Cliente 5, Abril/2019
		metricas = servico.getMetricas(5, 201904);
		Assert.assertEquals(2, metricas.length);

		metrica = metricas[1];
		Assert.assertEquals(Tipo.MRR, metrica.getTipo());
		Assert.assertEquals(399.0, metrica.getValor(), 0.0001);

		metrica = metricas[0];
		Assert.assertEquals(Tipo.Resurrected_MRR, metrica.getTipo());
		Assert.assertEquals(399, metrica.getValor(), 0.0001);

		

		// Cliente 4, Abril/2019
		metricas = servico.getMetricas(4, 201904);
		Assert.assertEquals(1, metricas.length);

		metrica = metricas[0];
		Assert.assertEquals(Tipo.Cancelled_MRR, metrica.getTipo());
		Assert.assertEquals(185.0, metrica.getValor(), 0.0001);

	}

	
	@Test
	public void testeParcelas() throws ParseException {

		ServicoMRR servico = new ServicoMRR();

		servico.pagamentoEfetuado(1, "10/01/2019", "Bronze/4", 400);
		servico.pagamentoEfetuado(2, "18/04/2019", "Platina/1", 399);

		DAO d = new DAO();
		d.begin();
		List<Parcela> listaParcelas = (List<Parcela>) d.get(Parcela.class);
	
		Assert.assertEquals(5, listaParcelas.size());

		//parcelas cliente 1
		listaParcelas = (List<Parcela>) d.get(Parcela.class,"clienteID = 1");
		Assert.assertEquals(4, listaParcelas.size());

		Date d1 = DF.parse("10/01/2019");
		Date d2 = DF.parse( "10/02/2019");
		Date d3 = DF.parse("10/03/2019");
		Date d4 = DF.parse( "10/04/2019");
		
		Parcela p = listaParcelas.get(0);
		Assert.assertEquals(1, p.getClienteID());
		Assert.assertEquals(d1, p.getData());
		Assert.assertEquals(100, p.getValor(),0.0001);
		Assert.assertEquals(201901, p.getMes());
		
	    p = listaParcelas.get(1);
		Assert.assertEquals(1, p.getClienteID());
		Assert.assertEquals(d2, p.getData());
		Assert.assertEquals(100, p.getValor(),0.0001);
		Assert.assertEquals(201902, p.getMes());
		
	    p = listaParcelas.get(2);
		Assert.assertEquals(1, p.getClienteID());
		Assert.assertEquals(d3, p.getData());
		Assert.assertEquals(100, p.getValor(),0.0001);
		
	    p = listaParcelas.get(3);
		Assert.assertEquals(1, p.getClienteID());
		Assert.assertEquals(d4, p.getData());
		Assert.assertEquals(100, p.getValor(),0.0001);
		
		//parcelas cliente 2
		listaParcelas = (List<Parcela>) d.get(Parcela.class,"clienteID = 2");
		Assert.assertEquals(1, listaParcelas.size());

		 d1 = DF.parse("18/04/2019");
		  
		
	    p = listaParcelas.get(0);
		Assert.assertEquals(2, p.getClienteID());
		Assert.assertEquals(d1, p.getData());
		Assert.assertEquals(399, p.getValor(),0.0001);
		
	     
	
	    d.commit();
	}


}
