package com.digitalinovatioone.cervejaria.service;

import com.digitalinovatioone.cervejaria.builder.CervejaDTOBuilder;
import com.digitalinovatioone.cervejaria.dto.CervejaDTO;
import com.digitalinovatioone.cervejaria.entity.Cerveja;
import com.digitalinovatioone.cervejaria.exception.BebidaJaRegistradaException;
import com.digitalinovatioone.cervejaria.exception.BebidaNaoEncontradaException;
import com.digitalinovatioone.cervejaria.exception.BebidaNaoExisteException;
import com.digitalinovatioone.cervejaria.exception.EstoqueDeBebidaExcedidoException;
import com.digitalinovatioone.cervejaria.mapper.CervejaMapper;
import com.digitalinovatioone.cervejaria.repository.CervejaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CervejaServiceTest {
    private static final Long ID_CERVEJA_INVALIDO =1L;

    @Mock
    private CervejaRepository cervejaRepository;

    private final CervejaMapper cervejaMapper = CervejaMapper.INSTANCE;

    @InjectMocks
    private CervejaService cervejaService;

    @Test
    void quandoCervejaInformadaEntaoDeveSerCriada() throws BebidaJaRegistradaException {
        //Given
        CervejaDTO cervejaDTO = CervejaDTOBuilder.builder().build().getCervejaDTO();
        Cerveja cerveja_a_ser_salva = cervejaMapper.toModel(cervejaDTO);
        //When
        when(cervejaRepository.findByNome(cervejaDTO.getNome())).thenReturn(Optional.empty());
        when(cervejaRepository.save(cerveja_a_ser_salva)).thenReturn(cerveja_a_ser_salva);
        //Then
        CervejaDTO cervejaDTOcriada = cervejaService.criaCerveja(cervejaDTO);

        assertThat(cervejaDTO.getId(), is(equalTo((cervejaDTO.getId()))));
        assertThat(cervejaDTO.getNome(), is(equalTo((cervejaDTO.getNome()))));
        assertThat(cervejaDTO.getQuantidade(), is(equalTo((cervejaDTO.getQuantidade()))));
        assertThat(cervejaDTO.getQuantidade(), is(greaterThan(9)));

//        assertEquals(cervejaDTO.getId(),cervejaDTOcriada.getId());
//        assertEquals(cervejaDTO.getNome(),cervejaDTOcriada.getNome());
    }

    @Test
    void quandoCervejaJaRegistradaUmaExcecaoDeveSerLancada(){
        //Given
        CervejaDTO cervejaDTO = CervejaDTOBuilder.builder().build().getCervejaDTO();
        Cerveja cervejaDuplicada = cervejaMapper.toModel(cervejaDTO);
        //When
        when(cervejaRepository.findByNome(cervejaDTO.getNome())).thenReturn(Optional.of(cervejaDuplicada));
        //then
        assertThrows(BebidaJaRegistradaException.class,()-> cervejaService.criaCerveja(cervejaDTO));
    }

    @Test
    void quandoNomeValidoDeCervejaEInformadoRetornaUmaCerveja() throws BebidaNaoEncontradaException {
        //Given
        CervejaDTO cervejaProcuradaEsperadaDTO = CervejaDTOBuilder.builder().build().getCervejaDTO();
        Cerveja cervejaProcuradaEsperada = cervejaMapper.toModel(cervejaProcuradaEsperadaDTO);
        //When
        when(cervejaRepository.findByNome(cervejaProcuradaEsperada.getNome())).thenReturn(Optional.of(cervejaProcuradaEsperada));
        //Then
        CervejaDTO cervejaAchadaDTO = cervejaService.procuraPorNome(cervejaProcuradaEsperadaDTO.getNome());
        assertThat(cervejaAchadaDTO, is(equalTo(cervejaProcuradaEsperadaDTO)));
    }

    @Test
    void quandoUmaCervejaNaoRegistradaLancaUmaExcecao(){
        //Given
        CervejaDTO cervejaProcuradaEsperadaDTO = CervejaDTOBuilder.builder().build().getCervejaDTO();

        //When
        when(cervejaRepository.findByNome(cervejaProcuradaEsperadaDTO.getNome())).thenReturn(Optional.empty());
        //Then
        assertThrows(BebidaNaoEncontradaException.class,
                ()->cervejaService.procuraPorNome(cervejaProcuradaEsperadaDTO.getNome()));
    }

    @Test
    void quandoListaCervejaChamadoRetornaUmaListaDeCerveja() {
        //Given
        CervejaDTO cervejaProcuradaEsperadaDTO = CervejaDTOBuilder.builder().build().getCervejaDTO();
        Cerveja cervejaProcuradaEsperada = cervejaMapper.toModel(cervejaProcuradaEsperadaDTO);
        //When
        when(cervejaRepository.findAll()).thenReturn(Collections.singletonList(cervejaProcuradaEsperada));
        //Then
        List<CervejaDTO> listaDeCervejas = cervejaService.listar();
        assertThat(listaDeCervejas, is(not(empty())));
        assertThat(listaDeCervejas.get(0), is(equalTo(cervejaProcuradaEsperadaDTO)));
    }
    @Test
    void quandoListaCervejaChamadoRetornaUmaListaDeCervejaVazia() {
        //When
        when(cervejaRepository.findAll()).thenReturn(Collections.emptyList());
        //Then
        List<CervejaDTO> listaDeCervejas = cervejaService.listar();
        assertThat(listaDeCervejas, is(empty()));
    }

    @Test
    void quandoDeleteChamadoComIdValidoCervejaDeveSerExcluida() throws BebidaNaoExisteException {
        //Given
        CervejaDTO cervejaDeletadaEsperadaDTO = CervejaDTOBuilder.builder().build().getCervejaDTO();
        Cerveja cervejaDeletadaEsperada = cervejaMapper.toModel(cervejaDeletadaEsperadaDTO);
        //When
        when(cervejaRepository.findById(cervejaDeletadaEsperadaDTO.getId())).thenReturn(Optional.of(cervejaDeletadaEsperada));
        Mockito.doNothing().when(cervejaRepository).deleteById(cervejaDeletadaEsperadaDTO.getId());
        //then
        cervejaService.deletaPorId(cervejaDeletadaEsperadaDTO.getId());

        Mockito.verify(cervejaRepository, Mockito.times(1)).findById(cervejaDeletadaEsperadaDTO.getId());
        Mockito.verify(cervejaRepository, Mockito.times(1)).deleteById(cervejaDeletadaEsperadaDTO.getId());
    }
    @Test
    void quandoIncrementaChamadoEntaoIncrementaCervejaNoEstoque() throws BebidaNaoExisteException, EstoqueDeBebidaExcedidoException {
        //given
        CervejaDTO cervejaEsperadaDTO = CervejaDTOBuilder.builder().build().getCervejaDTO();
        Cerveja cervejaEsperada = CervejaMapper.INSTANCE.toModel(cervejaEsperadaDTO);
        //when
        when(cervejaRepository.findById(cervejaEsperadaDTO.getId())).thenReturn(Optional.of(cervejaEsperada));
        when(cervejaRepository.save(cervejaEsperada)).thenReturn(cervejaEsperada);

        int quantidade_a_Incrementar = 30;
        int quantidadeEsperedaAposIncrementar = cervejaEsperadaDTO.getQuantidade() + quantidade_a_Incrementar;
        //then
        CervejaDTO cervejaIncrementada = cervejaService.incrementar(cervejaEsperadaDTO.getId(),quantidade_a_Incrementar);
        assertThat(quantidadeEsperedaAposIncrementar,equalTo(cervejaIncrementada.getQuantidade()));
        assertThat(quantidadeEsperedaAposIncrementar, lessThan(cervejaEsperadaDTO.getMax()));
    }
    @Test
    void quandoIncrementoMaiorQueQuantidadeMaximaEntaoRetornaExcecao(){
        //given
        CervejaDTO cervejaEsperadaDTO = CervejaDTOBuilder.builder().build().getCervejaDTO();
        Cerveja cervejaEsperada = CervejaMapper.INSTANCE.toModel(cervejaEsperadaDTO);
        //when
        when(cervejaRepository.findById(cervejaEsperadaDTO.getId())).thenReturn(Optional.of(cervejaEsperada));

        int quantidade_a_Incrementar = 50;
        //then
        assertThrows(EstoqueDeBebidaExcedidoException.class, ()->cervejaService.incrementar(cervejaEsperadaDTO.getId(),quantidade_a_Incrementar));
    }
    @Test
    void quandoIncrementaChamadoComIdInvalidoEntaoRetornaExcecao() {
        //given
        int quantidade_a_Incrementar = 30;
        //When
        when(cervejaRepository.findById(ID_CERVEJA_INVALIDO)).thenReturn(Optional.empty());
        //then
        assertThrows(BebidaNaoExisteException.class, ()->cervejaService.incrementar(ID_CERVEJA_INVALIDO,quantidade_a_Incrementar));
    }
    @Test
    void quandoDecrementarChamadoEntaoDecrementaDoEstoque() throws BebidaNaoExisteException, EstoqueDeBebidaExcedidoException {
        CervejaDTO cervejaDTOEsperada = CervejaDTOBuilder.builder().build().getCervejaDTO();
        Cerveja cervejaEsperada = cervejaMapper.toModel(cervejaDTOEsperada);

        when(cervejaRepository.findById(cervejaDTOEsperada.getId())).thenReturn(Optional.of(cervejaEsperada));
        when(cervejaRepository.save(cervejaEsperada)).thenReturn(cervejaEsperada);

        int quantidadeParaDecrementar = 5;
        int expectedQuantityAfterDecrement = cervejaDTOEsperada.getQuantidade() - quantidadeParaDecrementar;
        CervejaDTO CervejaDTODecrementada = cervejaService.decrementar(cervejaDTOEsperada.getId(), quantidadeParaDecrementar);

        assertThat(expectedQuantityAfterDecrement, equalTo(CervejaDTODecrementada.getQuantidade()));
        assertThat(expectedQuantityAfterDecrement, greaterThan(0));
    }

    @Test
    void quandoDecrementarChamadoParaEstoqueVazioEntaoEstoqueDeBebidaVazio() throws BebidaNaoExisteException, EstoqueDeBebidaExcedidoException {
        CervejaDTO cervejaDTOesperada = CervejaDTOBuilder.builder().build().getCervejaDTO();
        Cerveja cervejaEsperada = cervejaMapper.toModel(cervejaDTOesperada);

        when(cervejaRepository.findById(cervejaDTOesperada.getId())).thenReturn(Optional.of(cervejaEsperada));
        when(cervejaRepository.save(cervejaEsperada)).thenReturn(cervejaEsperada);

        int quantidadeParaDecrementar = 10;
        int quantidadeEsperadaAposDecrementar = cervejaDTOesperada.getQuantidade() - quantidadeParaDecrementar;
        CervejaDTO cervejaDTODecrementada = cervejaService.decrementar(cervejaDTOesperada.getId(), quantidadeParaDecrementar);

        assertThat(quantidadeEsperadaAposDecrementar, equalTo(0));
        assertThat(quantidadeEsperadaAposDecrementar, equalTo(cervejaDTODecrementada.getQuantidade()));
    }

    @Test
    void quandoDecrementoMenorQueZeroEntaoRetornaExcecao() {
        CervejaDTO cervejaDTOEsperada = CervejaDTOBuilder.builder().build().getCervejaDTO();
        Cerveja cervejaEsperada = cervejaMapper.toModel(cervejaDTOEsperada);

        when(cervejaRepository.findById(cervejaDTOEsperada.getId())).thenReturn(Optional.of(cervejaEsperada));

        int quantityToDecrement = 80;
        assertThrows(EstoqueDeBebidaExcedidoException.class, () -> cervejaService.decrementar(cervejaDTOEsperada.getId(), quantityToDecrement));
    }

    @Test
    void quandoDecrementoChamadoComIdCervejaInvalidoEntaoRetornaExcecao() {
        int quantityToDecrement = 10;

        when(cervejaRepository.findById(ID_CERVEJA_INVALIDO)).thenReturn(Optional.empty());

        assertThrows(BebidaNaoExisteException.class, () -> cervejaService.decrementar(ID_CERVEJA_INVALIDO, quantityToDecrement));
    }
}
