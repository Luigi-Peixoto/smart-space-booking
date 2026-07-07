import { useNavigate } from 'react-router-dom';
import SSBLogo from '../../assets/SSBLogo.png';
import './AcessoNegado.css';

function AcessoNegado() {
  const navigate = useNavigate();

  return (
    <div className="acesso-negado-container">
      <div className="acesso-negado-box">
        <img className="logo" src={SSBLogo} alt="Logo Smart Item Booking" />
        
        <div className="error-header">
          <h1 className="error-code">403</h1>
          <h2 className="error-title">Acesso Restrito</h2>
        </div>
        
        <p className="error-message">
          Você não possui os privilégios necessários para visualizar esta página. 
          Isso ocorre porque a área solicitada é exclusiva para perfis administrativos.
        </p>

        <p className="error-support">
          Se você acredita que isso é um erro, por favor, contate o administrador do sistema.
        </p>

        <button 
          className="back-button" 
          onClick={() => navigate('/home')}
        >
          Voltar para o Catálogo
        </button>
      </div>
    </div>
  );
}

export default AcessoNegado;