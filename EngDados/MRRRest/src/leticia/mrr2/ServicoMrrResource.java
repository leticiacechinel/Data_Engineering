package leticia.mrr2;

 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import leticia.mrr.MetricaCliente;
import leticia.mrr.ServicoMRR;

 

@Singleton

@Path ("mrr")
public class ServicoMrrResource {
 	 
	private ServicoMRR servico = new ServicoMRR();

	@POST
	@Path("/upload")
	@Consumes("text/plain")
 	public void carregaBanco (String base) throws IOException, ParseException {
		BufferedReader br = new BufferedReader(new StringReader(base));
		String linha;
		int i=0;
		while ((linha=br.readLine())!=null) {
			 String[] t = getTokens(linha);		
			 int cliente = Integer.parseInt(t[0]);
			 String data =  t[1];
			 double valor = Double.parseDouble(t[2]);
			 String plano = t[3];
			 if (i++%100==0){
				 System.out.println(i);
			 }
			 servico.pagamentoEfetuado(cliente, data, plano, valor);
		}
	}
	
	@GET
	@Path("metricas/{cliente}/{mes}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public MetricaCliente[] getMetrica (@PathParam("cliente")int cliente, @PathParam("mes")int mes) {
		try {
		return  servico.getMetricas(cliente, mes);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	 
	@GET 
	public void teste() {
		System.out.println();
	}
	 




	private static String[] getTokens(String linha) {
		String[] ts = linha.split("\\,");
		String[] ret = new String[4];
		
		ret[0] = ts[0];
		ret[1] = ts[1];
		
		String aux = ts[2].substring(4)+"."+ts[3];
		ret[2] = aux.substring(0, aux.length()-1); //tira aspa do fim
		
		ret[3] = ts[4];
		
		return ret;
	}
}
