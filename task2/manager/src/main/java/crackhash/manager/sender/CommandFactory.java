package crackhash.manager.sender;

/*
  SendCommand - отослать задачу воркеру
  ResendCommand - Отослать задачу другому воркеру, тк первый воркер упал
  ResendCreatedCommand - Отослать задачки, которые менеджер не успел отправил и упал
  ResendZeroPartsCommand - Отослать задачи, которые не были разосланы из-за того, что все воркеры были мертвы
 */
public interface CommandFactory {
  Command getSendCommand();
  Command getResendCommand(int workerNum);
  Command getResendCreatedCommand();
  Command getResendZeroPartsCommand();
}