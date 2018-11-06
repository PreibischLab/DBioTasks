package main.java.com.tools.server;

import java.util.UUID;

public class Login {

	private final String id;
	private final Account account;
	private final ServerConfiguration server;

	public String getId() {
		return id;
	}

	public Account getAccount() {
		return account;
	}

	public ServerConfiguration getServer() {
		return server;
	}

	private Login(Builder builder) {
		this.id = builder.id;
		this.account = builder.account;
		this.server = builder.server;
	}

	public static class Builder {

		private String id;
		private Account account;
		private ServerConfiguration server;

		public Builder id(final String id) {
			this.id = id;
			return this;
		}
		
		public Builder id() {
			this.id = UUID.randomUUID().toString().replace("-", "");
			return this;
		}

		public Builder account(final Account account) {
			this.account = account;
			return this;
		}

		public Builder server(final ServerConfiguration server) {
			this.server = server;
			return this;
		}

		public Login build() {
			return new Login(this);
		}

	}

}
