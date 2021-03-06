package de.kumpelblase2.remoteentities.api.thinking.goals;

import java.util.Iterator;
import java.util.List;
import net.minecraft.server.v1_7_R1.EntityVillager;
import net.minecraft.server.v1_7_R1.Vec3D;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import de.kumpelblase2.remoteentities.api.RemoteEntity;
import de.kumpelblase2.remoteentities.api.thinking.DesireBase;
import de.kumpelblase2.remoteentities.api.thinking.DesireType;
import de.kumpelblase2.remoteentities.exceptions.NotAVillagerException;
import de.kumpelblase2.remoteentities.nms.RandomPositionGenerator;
import de.kumpelblase2.remoteentities.persistence.ParameterData;
import de.kumpelblase2.remoteentities.persistence.SerializeAs;
import de.kumpelblase2.remoteentities.utilities.ReflectionUtil;

/**
 * Using this desire the villager baby will play around with another baby villager.
 */
public class DesirePlay extends DesireBase
{
	protected EntityVillager m_villager;
	protected EntityVillager m_friend;
	protected int m_playTick;
	@SerializeAs(pos = 1)
	protected double m_speed;

	@Deprecated
	public DesirePlay(RemoteEntity inEntity)
	{
		super(inEntity);
		if(!(this.getEntityHandle() instanceof EntityVillager))
			throw new NotAVillagerException();

		this.m_villager = (EntityVillager)this.getEntityHandle();
		this.m_type = DesireType.PRIMAL_INSTINCT;
	}

	public DesirePlay()
	{
		this(-1);
	}

	public DesirePlay(double inSpeed)
	{
		super();
		this.m_type = DesireType.PRIMAL_INSTINCT;
		this.m_speed = inSpeed;
	}

	@Override
	public void onAdd(RemoteEntity inEntity)
	{
		super.onAdd(inEntity);
		if(!(this.getEntityHandle() instanceof EntityVillager))
			throw new NotAVillagerException();

		this.m_villager = (EntityVillager)this.getEntityHandle();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean shouldExecute()
	{
		if(this.getEntityHandle() == null)
			return false;

		if(this.m_villager.getAge() >= 0)
			return false;
		else if(this.m_villager.aI().nextInt(400) != 0)
			return false;
		else
		{
			List villagers = this.m_villager.world.a(EntityVillager.class, this.m_villager.boundingBox.grow(6, 3, 6));
			double minDist = Double.MAX_VALUE;
			Iterator it = villagers.iterator();

			while(it.hasNext())
			{
				EntityVillager villager = (EntityVillager)it.next();
				if(villager != this.m_villager && !villager.bZ() && villager.getAge() < 0)
				{
					double dist = villager.e(this.m_villager);

					if(dist <= minDist)
					{
						minDist = dist;
						this.m_friend = villager;
					}
				}
			}

			if(this.m_friend == null)
			{
				Vec3D vec = RandomPositionGenerator.a(this.m_villager, 16, 3);

				if(vec == null)
					return false;

				Vec3D.a.release(vec);
			}

			return true;
		}
	}

	@Override
	public boolean canContinue()
	{
		return this.m_playTick > 0;
	}

	@Override
	public void startExecuting()
	{
		if(this.m_friend != null)
			this.m_villager.j(true);

		this.m_playTick = 1000;
	}

	@Override
	public void stopExecuting()
	{
		this.m_villager.j(false);
		this.m_friend = null;
	}

	@Override
	public boolean update()
	{
		this.m_playTick--;
		if(this.m_friend != null)
		{
			if(this.m_villager.e(this.m_friend) > 4)
				this.getRemoteEntity().move((LivingEntity)this.m_friend.getBukkitEntity(), (this.m_speed == -1 ? this.getRemoteEntity().getSpeed() : this.m_speed));
		}
		else if(this.getNavigation().g())
		{
			Vec3D vec = RandomPositionGenerator.a(this.m_villager, 16, 3);

			if(vec == null)
				return true;

			this.getRemoteEntity().move(new Location(this.getRemoteEntity().getBukkitEntity().getWorld(), vec.c, vec.d, vec.e), (this.m_speed == -1 ? this.getRemoteEntity().getSpeed() : this.m_speed));
			Vec3D.a.release(vec);
		}
		return true;
	}

	@Override
	public ParameterData[] getSerializableData()
	{
		return ReflectionUtil.getParameterDataForClass(this).toArray(new ParameterData[0]);
	}
}