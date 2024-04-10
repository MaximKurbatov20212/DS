package crackhash.manager.sender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class CommandFactoryImpl implements CommandFactory {
  private final PartSender sender;
  private final Command resendCommand;

  @Autowired
  CommandFactoryImpl(PartSender sender){
    this.sender = sender;

    resendCommand = () -> {
      try {
        sender.sendParts();
      } catch (Exception e){
        System.out.println("Command execution fail: " + e.getLocalizedMessage());
      }
    };
  }

  @Override
  public Command getSendCommand() {
    return resendCommand;
  }

  @Override
  public Command getResendCommand(int workerNum) {
    return () -> sender.resendKicked(workerNum);
  }

  /*
    Повторно послать сообщение всем воркерам, тк менеджер упал и не успел отправить
   */
  @Override
  public Command getResendCreatedCommand() {
    return sender::resendCreated;
  }

  /*
    Повторно послать сообщение всем задачам, у которых не было живых воркеров
   */
  @Override
  public Command getResendZeroPartsCommand() {
    return sender::resendZeroParts;
  }
  
}
