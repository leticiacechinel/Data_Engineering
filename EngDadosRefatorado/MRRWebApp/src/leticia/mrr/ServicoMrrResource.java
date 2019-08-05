package leticia.mrr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import leticia.cliente.Cliente;
import leticia.financeiro.Pagamento;
import leticia.mrr.Metrica;
import leticia.mrr.ServicoMRR;

@Singleton

@Path("/mrr")
public class ServicoMrrResource {

	private ServicoMRR servico = new ServicoMRR();

	@POST
	@Path("/upload")
	@Consumes("text/plain")
	public void carregaBanco(String base) throws IOException, ParseException {

		Reader r = new StringReader(base);

		BufferedReader br = new BufferedReader(r);

		try {
			upload(br);
		} finally {
			br.close();
		}
	}

	@POST
	@Path("/pagamentoEfetuado")
	@Consumes(MediaType.APPLICATION_JSON)
	public void pagamentoEfetuado(PagamentoUpload pagamento) throws ParseException {
		servico.pagamentoEfetuado(pagamento.getCliente(), pagamento.getData(), pagamento.getPlano(),
				pagamento.getValor());
	}

	@POST
	@Path("/clientes")
	@Consumes(MediaType.APPLICATION_JSON)
	public void sendClientes(Cliente[] clientes) throws ParseException {
		servico.uploadClientes(clientes);
	}

	private void upload(BufferedReader br) throws IOException, ParseException {
		String linha;
		 
		
		List<Pagamento> lista = new ArrayList<Pagamento>();
		while ((linha = br.readLine()) != null) {
			String[] tokens = getTokens(linha);

			int cliente = Integer.parseInt(tokens[0]);
			String data = tokens[1];
			double valor = Double.parseDouble(tokens[2]);
			String plano = tokens[3];
			
			lista.add(new Pagamento (cliente, data, plano, valor));

			 

		}
		
		servico.uploadPagamentos(lista);
	}

	@POST
	@Path("/fileUpload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public void carregaArquivo(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException, ParseException {

		Reader r = new InputStreamReader(uploadedInputStream);

		BufferedReader br = new BufferedReader(r);
		try {
			upload(br);
		} finally {
			br.close();
		}

	}

	@GET
	@Path("metricas/cliente/{cliente}/{mes}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Metrica[] getMetrica(@PathParam("cliente") int cliente, @PathParam("mes") int mes) {
		return servico.getMetricas(cliente, mes);
	}
 
	@POST
	@Path("metricas/cidade")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Metrica[] getMetricaCidade(NomeMesJson nomeMes) {
		return servico.getMetricasCidade(nomeMes.getNome(), nomeMes.getMes());
	}

	@GET
	@Path("metricas/cidade")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Metrica[] getMetrica(NomeMesJson nomeMes) {
		return servico.getMetricasCidade(nomeMes.getNome(), nomeMes.getMes());
	}
 
	@GET
	@Path("metricas/estado/{estado}/{mes}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Metrica[] getMetricaEstado(@PathParam("estado") String estado, @PathParam("mes") int mes) {
		return servico.getMetricasEstado(estado, mes);
	}

	@POST
	@Path("metricas/segmento")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Metrica[] getMetricaSegmentoPost(NomeMesJson nomeMes) {
		return servico.getMetricasSegmento(nomeMes.getNome(), nomeMes.getMes());
	}
	
	
	@GET
	@Path("metricas/segmento")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Metrica[] getMetricaSegmento(NomeMesJson nomeMes) {
		return servico.getMetricasSegmento(nomeMes.getNome(), nomeMes.getMes());
	}
	
	
	@GET
	@Path("metricas/empresa/{mes}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Metrica[] getMetricasEmpresa( @PathParam("mes") int mes) {
		return servico.getMetricasEmpresa(mes);
	}

	private static String[] getTokens(String linha) {
		String[] ts = linha.split("\\,");
		String[] ret = new String[4];

		ret[0] = ts[0];
		ret[1] = ts[1];

		String aux = ts[2].substring(4) + "." + ts[3];
		ret[2] = aux.substring(0, aux.length() - 1); // tira aspa do fim

		ret[3] = ts[4];

		return ret;
	}
	
	
	
}
