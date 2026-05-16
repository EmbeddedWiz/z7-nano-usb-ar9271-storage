APP = ath9k-firmware

# Add any other object files to this list below
APP_OBJS = ath9k-firmware.o

all: build

build: $(APP)

$(APP): $(APP_OBJS)
	$(CC) $(LDFLAGS) -o $@ $(APP_OBJS) $(LDLIBS)

